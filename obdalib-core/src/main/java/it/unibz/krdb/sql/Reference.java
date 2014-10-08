package it.unibz.krdb.sql;

/*
 * #%L
 * ontop-obdalib-core
 * %%
 * Copyright (C) 2009 - 2014 Free University of Bozen-Bolzano
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

/**
 * A utility class to store foreign key information.
 */
public class Reference {
	private String fkReferenceName;
	private String pkSchemaReference;
	private String pkTableReference;
	private String pkColumnReference;
	
	public Reference(String fkReferenceName, String pkSchemaReference, String pkTableReference, String pkColumnReference) {
		this.fkReferenceName = fkReferenceName;
		this.pkSchemaReference = pkSchemaReference;
		this.pkTableReference = pkTableReference;
		this.pkColumnReference = pkColumnReference;
	}
	
	public String getReferenceName() {
		return fkReferenceName;
	}
	

	public String getSchemaReference() {
		return pkSchemaReference;
	}
	
	
	public String getTableReference() {
		return pkTableReference;
	}
	
	public String getColumnReference() {
		return pkColumnReference;
	}
	
	@Override
	public String toString() {
		String msg = String.format("%s : %s(%s)", fkReferenceName, pkTableReference, pkColumnReference);
		return msg;
	}
}
