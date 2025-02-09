package org.gottschd.customizedrequestbody;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.gottschd.customizedrequestbody.model.MyResult;
import org.gottschd.customizedrequestbody.model.plain.MyPlainPayload;
import org.gottschd.customizedrequestbody.model.trimmedbyclass.MyTrimmedByClassPayload;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
public class MyController {

	@PostMapping("/untrimmed")
	public ResponseEntity<MyResult> untrimmed(@RequestBody @Valid MyPlainPayload myPlainPayload, Errors errors) {
		log.info("untrimmed - payload: {}, error: {}", myPlainPayload, errors);
		if( errors.hasErrors() ) {
			return ResponseEntity.ok(MyResult.builder().result(errors.getAllErrors().get(0).getDefaultMessage()).build());
		}
		return ResponseEntity.ok(MyResult.builder().result(myPlainPayload.getVorname()).build());
	}

	@PostMapping("/trimmedByClass")
	public ResponseEntity<MyResult> trimmedByClass(@RequestBody @Valid MyTrimmedByClassPayload myPayload, Errors errors) {
		log.info("trimmedByClass - payload: {}, error: {}", myPayload, errors);
		if( errors.hasErrors() ) {
			return ResponseEntity.ok(MyResult.builder().result(errors.getAllErrors().get(0).getDefaultMessage()).build());
		}
		return ResponseEntity.ok(MyResult.builder().result(myPayload.getVorname()).build());
	}

	@PostMapping("/trimmedByCustomAnnotation")
	public ResponseEntity<MyResult> trimmedByAnnotation(@TrimmedRequestBody @Valid MyPlainPayload myPlainPayload, Errors errors) {
		log.info("trimmedByCustomAnnotation - payload: {}, error: {}", myPlainPayload, errors);
		if( errors.hasErrors() ) {
			return ResponseEntity.ok(MyResult.builder().result(errors.getAllErrors().get(0).getDefaultMessage()).build());
		}
		return ResponseEntity.ok(MyResult.builder().result(myPlainPayload.getVorname()).build());
	}

}
