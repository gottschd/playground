package org.gottschd.customizedrequestbody.model.untrimmed;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class MyPayload {

	@Valid
	@NotBlank
	private String vorname;

}
