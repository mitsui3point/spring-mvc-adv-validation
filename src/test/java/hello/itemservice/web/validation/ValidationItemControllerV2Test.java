package hello.itemservice.web.validation;

import hello.itemservice.domain.item.Item;
import hello.itemservice.domain.item.ItemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;

import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(value = {ValidationItemControllerV2.class})
public class ValidationItemControllerV2Test {
    @Autowired
    private MockMvc mvc;

    @MockBean
    private ItemRepository itemRepository;

    @MockBean
    private ItemValidator itemValidator;

    @BeforeEach
    void setUp() {
        //https://www.baeldung.com/mockito-void-methods#partial-mocking
        doCallRealMethod().when(itemValidator).validate(isA(Item.class), isA(BeanPropertyBindingResult.class));
    }

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
        //when-then blank
        mvc.perform(post("/validation/v2/items/add")
                        .param("id", "1").param("price", "1000").param("quantity", "100")//valid
                        .param("itemName", "")
                ).andDo(print())
                .andExpect(status().isOk())
                .andExpect(view().name("validation/v2/addForm"))
                .andExpect(model().attributeHasFieldErrors("item", "itemName"))
        ;
        //when-then lowerPrice
        mvc.perform(post("/validation/v2/items/add")
                        .param("id", "1").param("itemName", "item1").param("quantity", "100")
                        .param("price", "999")
                ).andDo(print())
                .andExpect(status().isOk())
                .andExpect(view().name("validation/v2/addForm"))
                .andExpect(model().attributeHasFieldErrors("item", "price"))
        ;
        //when-then exceedPrice
        mvc.perform(post("/validation/v2/items/add")
                        .param("id", "1").param("itemName", "item1").param("quantity", "10")
                        .param("price", "1000001")
                ).andDo(print())
                .andExpect(status().isOk())
                .andExpect(view().name("validation/v2/addForm"))
                .andExpect(model().attributeHasFieldErrors("item", "price"))
        ;
        //when-then exceedQuantity
        mvc.perform(post("/validation/v2/items/add")
                        .param("id", "1").param("itemName", "item1").param("price", "2000")
                        .param("quantity", "10000")
                ).andDo(print())
                .andExpect(status().isOk())
                .andExpect(view().name("validation/v2/addForm"))
                .andExpect(model().attributeHasFieldErrors("item", "quantity"))
        ;
    }

    @Test
    void addItemComplexErrorTest() throws Exception {
        //when-then blank
        mvc.perform(post("/validation/v2/items/add")
                                .param("id", "1").param("itemName", "item1")
                ).andDo(print())
                .andExpect(status().isOk())
                .andExpect(view().name("validation/v2/addForm"))
                .andExpect(model().attributeHasErrors("item"))
                .andExpect(model().attributeExists("org.springframework.validation.BindingResult.item"))
        ;
        //when-then blank
        mvc.perform(post("/validation/v2/items/add")
                        .param("id", "1").param("itemName", "item1")
                        .param("price", "1000").param("quantity", "1")
                ).andDo(print())
                .andExpect(status().isOk())
                .andExpect(view().name("validation/v2/addForm"))
                .andExpect(model().attributeHasErrors("item"))
        ;
    }

    @Test
    void addItemBindingFailureTest() throws Exception {
        //when-then blank
        mvc.perform(post("/validation/v2/items/add")
                        .param("id", "1").param("itemName", "item1")
                        .param("price", "qqq").param("quantity", "1000")
                ).andDo(print())
                .andExpect(status().isOk())
                .andExpect(view().name("validation/v2/addForm"))
                .andExpect(model().attributeHasFieldErrors("item", "price"))
        ;
    }
}
