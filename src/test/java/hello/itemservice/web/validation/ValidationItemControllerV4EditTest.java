package hello.itemservice.web.validation;

import hello.itemservice.domain.item.Item;
import hello.itemservice.domain.item.ItemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.text.MessageFormat;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = {ValidationItemControllerV4.class})
public class ValidationItemControllerV4EditTest {
    private MockMvc mvc;

    @SpyBean
    private ItemRepository itemRepository;

    private Item savedItem;

    @BeforeEach
    void setUp() {
        /**
         * https://stackoverflow.com/questions/61224791/mockbean-not-initializing-service-bean-when-using-mockmvc-standalonesetup
         */
        mvc = MockMvcBuilders
                .standaloneSetup(new ValidationItemControllerV4(itemRepository))
                .build();
        //given
        savedItem = getEditTestData();
    }

    private Item addEditTestData() {
        return itemRepository.save(new Item("editItemTest", 10000, 100));
    }

    private Item getEditTestData() {
        return itemRepository.findAll()
                .stream()
                .filter(item -> item.getItemName().equals("editItemTest"))
                .findFirst()
                .orElseGet(() -> addEditTestData());
    }

    @Test
    void editItemTest() throws Exception {
        //when
        ResultActions perform = mvc.perform(post(MessageFormat.format("/validation/v4/items/{0}/edit", savedItem.getId().toString()))
                .param("itemName", "item1")
                .param("price", "1000")
                .param("quantity", "10")
                .param("id", savedItem.getId().toString())
        );
        //then
        perform.andDo(print())
                .andExpect(status().is3xxRedirection())
                .andExpect(result -> {
                    Item item = (Item) result.getModelAndView()
                            .getModel()
                            .get("item");
                    redirectedUrl(MessageFormat.format("/validation/v4/items/{0}", item.getId()));
                })
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
    void editItemFieldBlankErrorTest() throws Exception {
        //when-then blank
        mvc.perform(post(MessageFormat.format("/validation/v4/items/{0}/edit", savedItem.getId().toString()))
                        .param("id", "1").param("price", "1000").param("quantity", "100")//valid
                        .param("itemName", " ")
                ).andDo(print())
                .andExpect(status().isOk())
                .andExpect(view().name("validation/v4/editForm"))
                .andExpect(model().attributeHasFieldErrors("item", "itemName"))
        ;
    }
    @Test
    void editItemMinPriceErrorTest() throws Exception {
        //when-then lowerPrice
        mvc.perform(post(MessageFormat.format("/validation/v4/items/{0}/edit", savedItem.getId().toString()))
                        .param("id", "1").param("itemName", "item1").param("quantity", "100")
                        .param("price", "999")
                ).andDo(print())
                .andExpect(status().isOk())
                .andExpect(view().name("validation/v4/editForm"))
                .andExpect(model().attributeHasFieldErrors("item", "price"))
        ;
    }
    @Test
    void editItemFieldMaxPriceErrorTest() throws Exception {
        //when-then exceedPrice
        mvc.perform(post(MessageFormat.format("/validation/v4/items/{0}/edit", savedItem.getId().toString()))
                        .param("id", "1").param("itemName", "item1").param("quantity", "10")
                        .param("price", "1000001")
                ).andDo(print())
                .andExpect(status().isOk())
                .andExpect(view().name("validation/v4/editForm"))
                .andExpect(model().attributeHasFieldErrors("item", "price"))
        ;
    }
    @Test
    void editItemNullErrorTest() throws Exception {
        //when-then blank
        mvc.perform(post(MessageFormat.format("/validation/v4/items/{0}/edit", savedItem.getId().toString()))
                        .param("id", "1").param("itemName", "item1")
                ).andDo(print())
                .andExpect(status().isOk())
                .andExpect(view().name("validation/v4/editForm"))
                .andExpect(model().attributeHasErrors("item"))
                .andExpect(model().attributeExists("org.springframework.validation.BindingResult.item"))
        ;
    }
    @Test
    void editItemComplexErrorTest() throws Exception {
        //when-then blank
        mvc.perform(post(MessageFormat.format("/validation/v4/items/{0}/edit", savedItem.getId().toString()))
                        .param("id", "1").param("itemName", "item1")
                        .param("price", "1000").param("quantity", "1")
                ).andDo(print())
                .andExpect(status().isOk())
                .andExpect(view().name("validation/v4/editForm"))
                .andExpect(model().attributeHasErrors("item"))
        ;
    }

    @Test
    void editItemBindingFailureTest() throws Exception {
        //when-then blank
        mvc.perform(post(MessageFormat.format("/validation/v4/items/{0}/edit", savedItem.getId().toString()))
                        .param("id", "1").param("itemName", "item1")
                        .param("price", "qqq").param("quantity", "1000")
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(view().name("validation/v4/editForm"))
                .andExpect(model().attributeHasFieldErrors("item", "price"))
        ;
    }
}
