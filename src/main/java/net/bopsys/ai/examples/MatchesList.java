package net.bopsys.ai.examples;

import net.bopsys.ai.AssistantExample;

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
public class MatchesList {

	private List<String> matches;

	public AssistantExample<MatchesList> buildExample() {
		return new AssistantExample<>("3 european countries", new MatchesList(List.of("Spain", "Italy", "France")));
	}
}
