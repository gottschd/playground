package org.gottschd.customizedrequestbody.model.plain;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.jackson.Jacksonized;

@Getter
@Setter
@ToString
@Builder
@Jacksonized
public class MyPlainPayload {

	@Valid
	@NotBlank
	private String vorname;

}
