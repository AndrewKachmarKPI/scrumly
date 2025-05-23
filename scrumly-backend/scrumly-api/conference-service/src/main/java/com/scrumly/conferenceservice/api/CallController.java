package com.scrumly.conferenceservice.api;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@CrossOrigin
@RestController
@RequestMapping("/api/call")
public class CallController {

	@Value("${open-vidu.call.private-access}")
	private String CALL_PRIVATE_ACCESS;

	@GetMapping("/config")
	public ResponseEntity<?> getconfig() {
		Map<String, Object> response = new HashMap<String, Object>();
		response.put("isPrivate", CALL_PRIVATE_ACCESS.equals("ENABLED"));
		return new ResponseEntity<>(response, HttpStatus.OK);
	}
}
