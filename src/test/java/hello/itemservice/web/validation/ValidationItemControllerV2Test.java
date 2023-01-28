package hello.itemservice.web.validation;

import hello.itemservice.domain.item.Item;
import hello.itemservice.domain.item.ItemRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.HashMap;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(value = {ValidationItemControllerV2.class})
public class ValidationItemControllerV2Test {
    @Autowired
    private MockMvc mvc;

    @MockBean
    private ItemRepository itemRepository;

    @Test
    void addItemTest() throws Exception {
        //given
        Item saveItem = new Item("item1", 1000, 10);
        saveItem.setId(1L);
        //when
        when(itemRepository.save(saveItem))
                .thenReturn(saveItem);
        ResultActions perform = mvc.perform(post("/validation/v2/items/add")
                .param("itemName", "item1")
                .param("price", "1000")
                .param("quantity", "10")
                .param("id", "1")
        );
        //then
        perform.andDo(print())
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/validation/v2/items/1?status=true"))
        ;
    }

    /**
     * @FieldValidation : 필드 검증
     * <br>상품명: 필수, 공백X
     * <br>가격: 1000원 이상, 1백만원 이하
     * <br>수량: 최대 9999
     * @TypeValidation : 타입 검증
     * <br>가격, 수량에 문자가 들어가면 검증 오류 처리
     * @RangeValidation : 특정 필드의 범위를 넘어서는 검증
     * <br>가격 * 수량의 합은 10,000원 이상
     */
    @Test
    void addItemFieldErrorTest() throws Exception {
        //given
        HashMap<String, String> blankItemNameErrors = new HashMap<>();
        blankItemNameErrors.put("itemName", "상품 이름은 필수입니다.");
        //when-then blank
        mvc.perform(post("/validation/v2/items/add")
                        .param("id", "1").param("price", "1000").param("quantity", "100")
                        .param("itemName", "")
                ).andDo(print())
                .andExpect(status().isOk())
                .andExpect(view().name("validation/v2/addForm"))
                .andExpect(model().attribute("errors", blankItemNameErrors))
        ;
        //given
        HashMap<String, String> exceedPriceErrors = new HashMap<>();
        exceedPriceErrors.put("price", "가격은 1,000 ~ 1,000,000 까지 허용됩니다.");
        //when-then lowerPrice
        mvc.perform(post("/validation/v2/items/add")
                        .param("id", "1").param("itemName", "item1").param("quantity", "100")
                        .param("price", "999")
                ).andDo(print())
                .andExpect(status().isOk())
                .andExpect(view().name("validation/v2/addForm"))
                .andExpect(model().attribute("errors", exceedPriceErrors))
        ;
        //when-then exceedPrice
        mvc.perform(post("/validation/v2/items/add")
                        .param("id", "1").param("itemName", "item1").param("quantity", "10")
                        .param("price", "1000001")
                ).andDo(print())
                .andExpect(status().isOk())
                .andExpect(view().name("validation/v2/addForm"))
                .andExpect(model().attribute("errors", exceedPriceErrors))
        ;
        //given
        HashMap<String, String> exceedQuantityErrors = new HashMap<>();
        exceedQuantityErrors.put("quantity", "수량은 최대 9,999 까지 허용됩니다.");
        //when-then exceedQuantity
        mvc.perform(post("/validation/v2/items/add")
                        .param("id", "1").param("itemName", "item1").param("price", "2000")
                        .param("quantity", "10000")
                ).andDo(print())
                .andExpect(status().isOk())
                .andExpect(view().name("validation/v2/addForm"))
                .andExpect(model().attribute("errors", exceedQuantityErrors))
        ;
    }

    @Test
    void addItemComplexErrorTest() throws Exception {
        //given
        HashMap<String, String> nullErrors = new HashMap<>();
        nullErrors.put("price", "가격은 1,000 ~ 1,000,000 까지 허용됩니다.");
        nullErrors.put("quantity", "수량은 최대 9,999 까지 허용됩니다.");
        //when-then blank
        mvc.perform(post("/validation/v2/items/add")
                                .param("id", "1").param("itemName", "item1")
                ).andDo(print())
                .andExpect(status().isOk())
                .andExpect(view().name("validation/v2/addForm"))
                .andExpect(model().attribute("errors", nullErrors))
        ;
        //given
        HashMap<String, String> errors = new HashMap<>();
        errors.put("globalError", "가격 * 수량의 합은 10,000 이상이어야 합니다. 현재 값 = " + 1000 * 1);
        //when-then blank
        mvc.perform(post("/validation/v2/items/add")
                        .param("id", "1").param("itemName", "item1")
                        .param("price", "1000").param("quantity", "1")
                ).andDo(print())
                .andExpect(status().isOk())
                .andExpect(view().name("validation/v2/addForm"))
                .andExpect(model().attribute("errors", errors))
        ;
    }
}
