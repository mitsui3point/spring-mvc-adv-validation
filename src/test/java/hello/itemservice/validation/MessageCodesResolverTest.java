package hello.itemservice.validation;

import org.junit.jupiter.api.Test;
import org.springframework.validation.DefaultMessageCodesResolver;
import org.springframework.validation.MessageCodesResolver;

import static org.assertj.core.api.Assertions.assertThat;

public class MessageCodesResolverTest {
    MessageCodesResolver codesResolver = new DefaultMessageCodesResolver();

    @Test
    void resolveMessageCodesObjectTest() {
        //when
        String[] messageCodes = codesResolver.resolveMessageCodes("required", "item");
        //then
        /* new ObjectError("item",
        new String[]{"required.item", "required"},
        null, null)
        */
        assertThat(messageCodes).containsExactly(
                //1순위: code + "." + object name
                "required.item",
                //2순위: code
                "required");
    }

    @Test
    void resolveMessageCodesFieldTest() {
        //when
        String[] messageCodes = codesResolver.resolveMessageCodes("required", "item", "itemName", String.class);
        //then
        /* new FieldError("item",
        "itemName",
        item.getItemName(),
        false,
        new String[]{"required.item.itemName", "required.itemName", "required.java.lang.String", "required"},
        null,
        "default error message") */
        assertThat(messageCodes).containsExactly(
                //1순위: code + "." + object name + "." + field
                "required.item.itemName",
                //2순위: code + "." + field
                "required.itemName",
                //3순위: code + "." + field type
                "required.java.lang.String",
                //4순위: code
                "required"
        );
    }
}
