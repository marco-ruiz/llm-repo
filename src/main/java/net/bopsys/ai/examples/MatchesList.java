/*
 * Copyright 2026 Bopsys LLC & Marco Ruiz
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
