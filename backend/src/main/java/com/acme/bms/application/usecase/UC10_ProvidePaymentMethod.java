package com.acme.bms.application.usecase;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.acme.bms.api.auth.UpdatePaymentTokenRequest;
import com.acme.bms.api.auth.UserInfoResponse;
import com.acme.bms.application.exception.UserNotFoundException;
import com.acme.bms.domain.repo.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UC10_ProvidePaymentMethod {

	private final UserRepository users;

	@Transactional
	public UserInfoResponse execute(UpdatePaymentTokenRequest req, Long userId) {
		if (userId == null) throw new UserNotFoundException();

		var user = users.findById(userId).orElseThrow(UserNotFoundException::new);

		user.setPaymentToken(req.paymentToken());
		users.save(user);

		return new UserInfoResponse(
				user.getId(),
				user.getEmail(),
				user.getUsername(),
				user.getFullName(),
				user.getRole().name(),
				user.getPaymentToken());
	}

}
