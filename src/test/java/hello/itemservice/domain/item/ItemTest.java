package hello.itemservice.domain.item;


import org.junit.jupiter.api.Test;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.util.Set;

/**
 - 하이버네이트 Validator 관련 링크
 : 공식 사이트: http://hibernate.org/validator/
 : 공식 메뉴얼: https://docs.jboss.org/hibernate/validator/6.2/reference/en-US/html_single/
 : 검증 애노테이션 모음: https://docs.jboss.org/hibernate/validator/6.2/reference/en-US/html_single/#validator-defineconstraints-spec
 : validation @Test 예시: https://docs.jboss.org/hibernate/stable/validator/reference/en-US/html_single/?v=8.0#_validating_constraints
 */
public class ItemTest {
    @Test
    void validationTest() {
        //given
        /*  검증기 생성
            다음 코드와 같이 검증기를 생성한다. 이후 스프링과 통합하면 우리가 직접 이런 코드를 작성하지는
            않으므로, 이렇게 사용하는구나 정도만 참고하자.*/
        ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory();
        Validator validator = validatorFactory.getValidator();

        Item item = new Item(" ", 0, 10000);

        //when
        Set<ConstraintViolation<Item>> violations = validator.validate(item);

        //then
        violations.forEach(violation -> {
            System.out.println("violation = " + violation);
            System.out.println("violation.getMessage() = " + violation.getMessage());
        });
    }
}
