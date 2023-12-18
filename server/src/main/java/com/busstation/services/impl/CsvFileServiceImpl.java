package com.busstation.services.impl;

import java.io.*;
import java.util.List;

import com.busstation.common.Constant;
import com.busstation.entities.Ticket;
import com.busstation.repositories.TicketRepository;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.busstation.entities.Account;
import com.busstation.entities.Role;
import com.busstation.entities.User;
import com.busstation.exceptions.DataExistException;
import com.busstation.repositories.AccountRepository;
import com.busstation.repositories.RoleRepository;
import com.busstation.repositories.UserRepository;
import com.busstation.services.CsvFileService;

@Service
public class CsvFileServiceImpl implements CsvFileService {

	@Autowired
	private AccountRepository accountRepository;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private RoleRepository roleRepository;

	@Autowired
	private TicketRepository ticketRepository;

	private static final String FILE_PATH = Constant.EXCEL_PATH + "/tickets.xlsx";

	@Override
	public void exportUsesToCsv(Writer writer) {
		String[] HEADERS = { "full_name", "phone_number", "email", "address",
				"status" };
		CSVFormat csvFormat = CSVFormat.DEFAULT.builder().setHeader(HEADERS).build();

		List<User> users = userRepository.findAll();

		try (final CSVPrinter printer = new CSVPrinter(writer, csvFormat)) {
			for (User user : users) {
				printer.printRecord(user.getFullName(),
						user.getPhoneNumber(),
						user.getEmail(), user.getAddress(), user.getStatus());
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void importUserstoCsvFile(BufferedReader reader) {
		String[] HEADERS = { "full_name", "username", "password", "role_name", "phone_number", "email", "address",
				"status" };
		CSVFormat csvFormat = CSVFormat.DEFAULT.builder().setHeader(HEADERS).build();

		try (final CSVParser parser = new CSVParser(reader, csvFormat)) {
			for (CSVRecord record : parser) {
				if (record.getRecordNumber() == 1) {
					continue;
				}
				String fullName = record.get("full_name");
				String username = record.get("username");
				String password = record.get("password");
				String role_name = record.get("role_name");
				String phoneNumber = record.get("phone_number");
				String email = record.get("email");
				String address = record.get("address");
				String stt = record.get("status");
				boolean status = Boolean.parseBoolean(stt);

				if (accountRepository.existsByusername(username)) {
					throw new DataExistException("This user with username: " + username + " already exist");
				} else {
					Account account = new Account();
					account.setUsername(username);
					account.setPassword(password);
					Role role = roleRepository.findByName(role_name);
					if (role == null) {
						throw new DataExistException("This role with name: " + role_name + " not valid.");
					} else {
						account.setRole(role);
						accountRepository.save(account);
						User user = new User();
						user.setAccount(accountRepository.findById(account.getAccountId()).get());
						user.setFullName(fullName);
						user.setPhoneNumber(phoneNumber);
						user.setEmail(email);
						user.setAddress(address);
						user.setStatus(status);
						userRepository.save(user);
					}

				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	@Override
	public void exportTicketToCsv(Writer writer) {
		String[] HEADERS = { "ID", "Address Start", "Address End", "Price", "Pickup Location", "Drop Off Location" };
		CSVFormat csvFormat = CSVFormat.DEFAULT.builder().setHeader(HEADERS).build();

		List<Ticket> tickets = ticketRepository.findAll();

		try (final CSVPrinter printer = new CSVPrinter(writer, csvFormat)) {
			for (Ticket ticket : tickets) {
				printer.printRecord(ticket.getTicketId(), ticket.getAddressStart(),
						ticket.getAddressEnd(), ticket.getPrice(), ticket.getPickupLocation(),
						ticket.getDropOffLocation());
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


}