package pdp.web;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.Before;
import org.junit.Test;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.MapBindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import pdp.PdpPolicyException;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.*;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

public class ErrorControllerTest {

    private ErrorController subject;
    private ErrorAttributes errorAttributes;

    @Before
    public void before() {
        this.errorAttributes = mock(ErrorAttributes.class);

        Map<String, Object> result = new HashMap<>();
        result.put("exception", "exception");
        result.put("message", "message");

        when(errorAttributes.getErrorAttributes(any(), any())).thenReturn(result);

        this.subject = new ErrorController();
        ReflectionTestUtils.setField(subject, "errorAttributes", this.errorAttributes);
    }

    @Test
    public void testErrorWithDuplicatedName() throws Exception {
        HttpServletRequest request = new MockHttpServletRequest();

        when(errorAttributes.getError(any())).thenReturn(new PdpPolicyException("name", "duplicated"));

        assertResponse(request, BAD_REQUEST, "{name=duplicated}");
    }

    @Test
    public void testErrorWithBadInput() throws Exception {
        HttpServletRequest request = new MockHttpServletRequest();

        Map<?, ?> target = new HashMap<>();
        //target.put()
        BindingResult bindingResult = new MapBindingResult(target, "policy");
        bindingResult.addError(new FieldError("policy", "name", "required"));
        bindingResult.addError(new FieldError("policy", "policyXml", "required"));

        when(errorAttributes.getError(any())).thenReturn(new MethodArgumentNotValidException(mock(MethodParameter.class), bindingResult));

        assertResponse(request, INTERNAL_SERVER_ERROR, "{name=required, policyXml=required}");
    }

    private void assertResponse(HttpServletRequest request, HttpStatus httpStatus, String expectedBodyResponse) {
        ResponseEntity<Map<String, Object>> response = subject.error(request);

        assertEquals(httpStatus, response.getStatusCode());

        Map<String, Object> body = response.getBody();
        //there were details, so we don't expect the 'exception' and 'message' still in here
        assertFalse(body.containsKey("exception"));
        assertFalse(body.containsKey("message"));

        assertEquals(expectedBodyResponse, body.get("details").toString());
    }

}
