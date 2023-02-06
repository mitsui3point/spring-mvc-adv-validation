package hello.itemservice.web.validation.form;


import org.junit.jupiter.api.Test;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.util.Set;

public class ItemSaveFormTest {
    @Test
    void validationTest() {
        //given
        /*  검증기 생성
            다음 코드와 같이 검증기를 생성한다. 이후 스프링과 통합하면 우리가 직접 이런 코드를 작성하지는
            않으므로, 이렇게 사용하는구나 정도만 참고하자.*/
        ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory();
        Validator validator = validatorFactory.getValidator();

        ItemSaveForm itemSaveForm = new ItemSaveForm(" ", 0, 10000);

        //when
        Set<ConstraintViolation<ItemSaveForm>> violations = validator.validate(itemSaveForm);

        //then
        violations.forEach(violation -> {
            System.out.println("violation = " + violation);
            System.out.println("violation.getMessage() = " + violation.getMessage());
        });
    }
}
