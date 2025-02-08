package org.gottschd.customizedrequestbody;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.gottschd.customizedrequestbody.model.MyResult;
import org.gottschd.customizedrequestbody.model.trimmed.MyTrimmedPayload;
import org.gottschd.customizedrequestbody.model.untrimmed.MyPayload;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
public class MyController {

	@PostMapping("/untrimmed")
	public ResponseEntity<MyResult> untrimmed(@RequestBody @Valid MyPayload myPayload) {
		log.info("untrimmed payload: {}", myPayload);
		return ResponseEntity.ok(MyResult.builder().result(myPayload.getVorname()).build());
	}

	@PostMapping("/trimmedByClass")
	public ResponseEntity<MyResult> trimmedByClass(@RequestBody @Valid MyTrimmedPayload myPayload) {
		log.info("trimmedByClass payload: {}", myPayload);
		return ResponseEntity.ok(MyResult.builder().result(myPayload.getVorname()).build());
	}

	// @PostMapping("/testCustomized")
	// public ResponseEntity<Void> testCustomized(@RequestBody @Valid MyPayload myPayload)
	// {
	// log.info("Received payload: {}", myPayload);
	// return ResponseEntity.ok().build();
	// }

}
