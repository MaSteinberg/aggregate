/*
 * Copyright (C) 2010 University of Washington
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.opendatakit.aggregate.submission;

import java.util.ArrayList;
import java.util.List;

import org.opendatakit.aggregate.constants.ParserConsts;
import org.opendatakit.common.web.constants.BasicConsts;

/**
 * The submission key represents an XPath-style identification of a particular data element
 * on the server.  The base 
 * 
 * @author wbrunette@gmail.com
 * @author mitchellsundt@gmail.com
 * 
 */
public class SubmissionKey {
	private final String key;
	
	public SubmissionKey( String key ) {
		this.key = key;
	}
	
	/**
	 * Used by form deletion. 
	 * 
	 * @param formId
	 * @param modelVersion
	 * @param uiVersion
	 * @param topLevelGroupElementName
	 * @param uri
	 */
	public SubmissionKey( String formId, Long modelVersion, Long uiVersion,
							String topLevelGroupElementName, String uri ) {
		StringBuilder b = new StringBuilder();
		b.append(formId);
		b.append(SubmissionKeyPart.K_OPEN_BRACKET_VERSION_EQUALS);
		b.append(modelVersion);
		b.append(SubmissionKeyPart.K_AND_UI_VERSION_EQUALS);
		b.append(uiVersion);
		b.append(SubmissionKeyPart.K_CLOSE_BRACKET);
		b.append(SubmissionKeyPart.K_SLASH);
		b.append(topLevelGroupElementName);
		b.append(SubmissionKeyPart.K_OPEN_BRACKET_KEY_EQUALS);
		b.append(uri);
		b.append(SubmissionKeyPart.K_CLOSE_BRACKET);
		this.key = b.toString();
	}
	
	public String toString() {
		return key;
	}

	public final List<SubmissionKeyPart> splitSubmissionKey() {
		List<SubmissionKeyPart> parts = new ArrayList<SubmissionKeyPart>();
		String[] stringParts;
		// handle slashes in formId by looking backward for the first occurance of the version
		int idxBeginningVersion = key.lastIndexOf(SubmissionKeyPart.K_OPEN_BRACKET_VERSION_EQUALS);
		if ( idxBeginningVersion > 0 ) {
		  String firstPartFront = key.substring(0,idxBeginningVersion);
		  String remainder = key.substring(idxBeginningVersion);
		  stringParts = remainder.split(BasicConsts.FORWARDSLASH);
		  if ( firstPartFront.contains(ParserConsts.FORWARD_SLASH) ) {
		    firstPartFront = firstPartFront.replaceAll(ParserConsts.FORWARD_SLASH, ParserConsts.FORWARD_SLASH_SUBSTITUTION);
		  }
		  stringParts[0] = firstPartFront + stringParts[0];
		} else {
		  stringParts = key.split(BasicConsts.FORWARDSLASH);
		}
		for ( String s : stringParts ) {
			parts.add( new SubmissionKeyPart(s));
		}
		return parts;
	}
}