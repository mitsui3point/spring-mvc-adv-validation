package hello.itemservice.web.validation;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import hello.itemservice.web.validation.form.ItemSaveForm;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ValidationItemApiController.class)
public class ValidationItemApiControllerTest {
    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.standaloneSetup(new ValidationItemApiController())
                .build();
    }

    @Test
    void saveSuccessTest() throws Exception {
        ItemSaveForm itemSaveForm = new ItemSaveForm("item1", 1000, 1000);
        String content = new ObjectMapper().writeValueAsString(itemSaveForm);

        mvc.perform(post("/validation/api/items/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(content)
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.itemName").value(itemSaveForm.getItemName()))
                .andExpect(jsonPath("$.price").value(itemSaveForm.getPrice()))
                .andExpect(jsonPath("$.quantity").value(itemSaveForm.getQuantity()))
        ;
    }

    @Test
    void saveValidationFailTest() throws Exception {
        ItemSaveForm itemSaveForm = new ItemSaveForm();
        itemSaveForm.setItemName(" ");
        itemSaveForm.setPrice(0);

        String content = new ObjectMapper().writeValueAsString(itemSaveForm);

        mvc.perform(post("/validation/api/items/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(content)
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(this::validationFailAssertions)
        ;
    }

    private void validationFailAssertions(MvcResult result) throws JsonProcessingException, UnsupportedEncodingException {
        List<Map> results = new ObjectMapper().readValue(result.getResponse().getContentAsString(), List.class);
        List<String> fields = results
                .stream()
                .map(o -> o.get("field").toString())
                .sorted(String::compareTo)
                .collect(Collectors.toList());

        assertThat(fields).containsExactly("itemName", "price", "quantity");
    }

    @Test
    void saveHttpMessageConvertFailTest() throws Exception {
        //"{"itemName": " ", "price": "string", "quantity": 1000}"
        String jsonStringContent = "{\"itemName\": \" \", \"price\": \"string\", \"quantity\": 1000}";
        mvc.perform(post("/validation/api/items/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonStringContent)
                )
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(this::httpMessageConvertFailAssertions)
        ;
    }

    private void httpMessageConvertFailAssertions(MvcResult result) {
        Class<? extends Exception> resolvedException = result.getResolvedException().getClass();
        assertThat(resolvedException).isEqualTo(HttpMessageNotReadableException.class);
    }

}
