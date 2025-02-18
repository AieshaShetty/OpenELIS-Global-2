package org.openelisglobal.patient;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openelisglobal.BaseTestConfig;
import org.openelisglobal.patient.service.PatientService;
import org.openelisglobal.patient.valueholder.Patient;
import org.openelisglobal.person.service.PersonService;
import org.openelisglobal.person.valueholder.Person;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { BaseTestConfig.class, PatientTestConfig.class })
@TestPropertySource("classpath:common.properties")
@ActiveProfiles("test")
class PatientServiceTest {

	@Autowired
	PatientService patientService;

	@Autowired
	PersonService personService;

	@Before
	public void init() throws Exception {
	}

	@Test
	public void createPatient_shouldCreateNewPatient() throws Exception {
		String firstName = "John";
		String lastname = "Doe";
		String dob = "12/12/1992";
		String gender = "M";
		Patient pat = createPatient(firstName, lastname, dob, gender);

		Assert.assertEquals(0, patientService.getAllPatients().size());
		// save patient to the DB
		String patientId = patientService.insert(pat);
		Patient savedPatient = patientService.get(patientId);

		Assert.assertEquals(1, patientService.getAllPatients().size());
		Assert.assertEquals(firstName, savedPatient.getPerson().getFirstName());
		Assert.assertEquals(lastname, savedPatient.getPerson().getLastName());
		Assert.assertEquals(gender, savedPatient.getGender());
	}

	@Test
	public void getAllPatients_shouldGetAllPatients() throws Exception {
		Assert.assertEquals(1, patientService.getAllPatients().size());
	}

	@Test
	public void updatePatient_shouldUpdateExistingPatient() throws Exception {
		// Create a new patient
		String firstName = "John";
		String lastName = "Doe";
		String dob = "12/12/1992";
		String gender = "M";
		Patient pat = createPatient(firstName, lastName, dob, gender);
		String patientId = patientService.insert(pat);

		// Get the patient from the database
		Patient existingPatient = patientService.get(patientId);
		Assert.assertNotNull(existingPatient);

		// Update patient details
		String updatedFirstName = "Jane";
		existingPatient.getPerson().setFirstName(updatedFirstName);
		patientService.update(existingPatient);

		// Verify the updated details
		Patient updatedPatient = patientService.get(patientId);
		Assert.assertNotNull(updatedPatient);
		Assert.assertEquals(updatedFirstName, updatedPatient.getPerson().getFirstName());
		Assert.assertEquals(lastName, updatedPatient.getPerson().getLastName()); // Ensure other details remain unchanged
	}
	@Test
	public void deletePatient_shouldDeleteExistingPatient() throws Exception {
		// Create a new patient
		String firstName = "John";
		String lastName = "Doe";
		String dob = "12/12/1992";
		String gender = "M";
		Patient pat = createPatient(firstName, lastName, dob, gender);
		String patientId = patientService.insert(pat);

		// Get the patient from the database
		Patient existingPatient = patientService.get(patientId);
		Assert.assertNotNull(existingPatient);

		// Delete the patient
		patientService.delete(patientId);

		// Verify that the patient is deleted
		Patient deletedPatient = patientService.get(patientId);
		Assert.assertNull(deletedPatient);
	}

	@Test
	public void createPatientWithBearerToken() {
		String token = " ";


		RequestSpecification request = given()
				.contentType(ContentType.JSON)
				.header("Authorization", "Bearer " + token)
				.body("{ \"firstName\": \"John\", \"lastName\": \"Doe\", \"dob\": \"12/12/1992\", \"gender\": \"M\" }");

		// Send the POST request to create a patient
		request.when()
				.post("/api/patients")
				.then()
				.statusCode(201);
	}
	private Patient createPatient(String firstName, String LastName, String birthDate, String gender)
			throws ParseException {
		Person person = new Person();
		person.setFirstName(firstName);
		person.setLastName(LastName);
		personService.save(person);

		DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
		Date date = dateFormat.parse(birthDate);
		long time = date.getTime();
		Timestamp dob = new Timestamp(time);

		Patient pat = new Patient();
		pat.setPerson(person);
		pat.setBirthDate(dob);
		pat.setGender(gender);

		return pat;
	}

}
