/*
 * Copyright 2016 Florian Pollak (fpdevelop@gmail.com)
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package at.ac.tuwien.e0826357.cardioapp.commons.service;

public class ServiceException extends Exception {

	private static final long serialVersionUID = -4082534357162483026L;

	public ServiceException() {
	}

	public ServiceException(String detailMessage) {
		super(detailMessage);
	}

	public ServiceException(Throwable throwable) {
		super(throwable);
	}

	public ServiceException(String detailMessage, Throwable throwable) {
		super(detailMessage, throwable);
	}

}
