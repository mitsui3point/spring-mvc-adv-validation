package hello.itemservice.web.validation;

import hello.itemservice.domain.item.Item;
import hello.itemservice.domain.item.ItemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.text.MessageFormat;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = {ValidationItemControllerV3.class})
public class ValidationItemControllerV3AddTest {
    private MockMvc mvc;

    @SpyBean
    private ItemRepository itemRepository;

    @BeforeEach
    void setUp() {
        /**
         * https://stackoverflow.com/questions/61224791/mockbean-not-initializing-service-bean-when-using-mockmvc-standalonesetup
         */
        mvc = MockMvcBuilders
                .standaloneSetup(new ValidationItemControllerV3(itemRepository))
                .build();
    }

    @Test
    void addItemTest() throws Exception {
        //given
        //when
        ResultActions perform = mvc.perform(post("/validation/v3/items/add")
                .param("itemName", "item1")
                .param("price", "1000")
                .param("quantity", "10")
        );
        //then
        perform.andDo(print())
                .andExpect(status().is3xxRedirection())
                .andExpect(result -> {
                    String itemId = result.getModelAndView().getModel().get("itemId").toString();
                    redirectedUrl(MessageFormat.format("/validation/v3/items/{0}?status=true", itemId));
                })
//                .andExpect(redirectedUrl("/validation/v3/items/1?status=true"))
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
    void addItemFieldBlankErrorTest() throws Exception {
        //when-then blank
        mvc.perform(post("/validation/v3/items/add")
                        .param("price", "1000").param("quantity", "100")//valid
                        .param("itemName", " ")
                ).andDo(print())
                .andExpect(status().isOk())
                .andExpect(view().name("validation/v3/addForm"))
                .andExpect(model().attributeHasFieldErrors("item", "itemName"))
        ;
    }
    @Test
    void addItemMinPriceErrorTest() throws Exception {
        //when-then lowerPrice
        mvc.perform(post("/validation/v3/items/add")
                        .param("itemName", "item1").param("quantity", "100")
                        .param("price", "999")
                ).andDo(print())
                .andExpect(status().isOk())
                .andExpect(view().name("validation/v3/addForm"))
                .andExpect(model().attributeHasFieldErrors("item", "price"))
        ;
    }
    @Test
    void addItemFieldMaxPriceErrorTest() throws Exception {
        //when-then exceedPrice
        mvc.perform(post("/validation/v3/items/add")
                        .param("itemName", "item1").param("quantity", "10")
                        .param("price", "1000001")
                ).andDo(print())
                .andExpect(status().isOk())
                .andExpect(view().name("validation/v3/addForm"))
                .andExpect(model().attributeHasFieldErrors("item", "price"))
        ;
    }
    @Test
    void addItemFieldMaxQuantityErrorTest() throws Exception {
        //when-then exceedQuantity
        mvc.perform(post("/validation/v3/items/add")
                        .param("itemName", "item1").param("price", "2000")
                        .param("quantity", "10000")
                ).andDo(print())
                .andExpect(status().isOk())
                .andExpect(view().name("validation/v3/addForm"))
                .andExpect(model().attributeHasFieldErrors("item", "quantity"))
        ;
    }

    @Test
    void addItemNullErrorTest() throws Exception {
        //when-then blank
        mvc.perform(post("/validation/v3/items/add")
                        .param("itemName", "item1")
                ).andDo(print())
                .andExpect(status().isOk())
                .andExpect(view().name("validation/v3/addForm"))
                .andExpect(model().attributeHasErrors("item"))
                .andExpect(model().attributeExists("org.springframework.validation.BindingResult.item"))
        ;
    }
    @Test
    void addItemComplexErrorTest() throws Exception {
        //when-then blank
        mvc.perform(post("/validation/v3/items/add")
                        .param("itemName", "item1")
                        .param("price", "1000").param("quantity", "1")
                ).andDo(print())
                .andExpect(status().isOk())
                .andExpect(view().name("validation/v3/addForm"))
                .andExpect(model().attributeHasErrors("item"))
        ;
    }

    @Test
    void addItemBindingFailureTest() throws Exception {
        //when-then blank
        mvc.perform(post("/validation/v3/items/add")
                        .param("itemName", "item1")
                        .param("price", "qqq").param("quantity", "1000")
                ).andDo(print())
                .andExpect(status().isOk())
                .andExpect(view().name("validation/v3/addForm"))
                .andExpect(model().attributeHasFieldErrors("item", "price"))
        ;
    }

}
