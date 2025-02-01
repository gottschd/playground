package org.gottschd.customizedrequestbody;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;

@Getter
@Setter
@ToString
public class MyPayload {

	@Valid
	@NotBlank
	private String vorname;

}
