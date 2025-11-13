package com.acme.bms.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "Bill")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Bill {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	@ManyToOne
	@JoinColumn(name = "trip_id")
	private Trip trip;
	private double totalAmount = 0;
	private double baseFee = 0;
	private double usageCost = 0;
	private double electricCharge = 0;
	private double discountAmount = 0;

	private LocalDateTime createdAt;
	private boolean paid = false;
	private LocalDateTime paidAt;
	private String paymentTokenUsed;

}
