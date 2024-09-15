package net.bopsys.ai.examples.books;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Marco Ruiz
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Innovator {

	private String firstName;
	private String lastName;
	private List<String> creations;
}
