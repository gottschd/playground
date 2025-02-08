package org.gottschd.customizedrequestbody.model.trimmed;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class MyTrimmedPayload {

	@Valid
	@NotBlank
	private String vorname;

}
