
/*
 * Copyright 2013-2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

// tag::class[]

import org.springframework.cloud.contract.spec.Contract;

import java.util.function.Supplier;

class contract_soap_xml implements Supplier<Contract> {

	@Override
	public Contract get() {
		return Contract.make(c -> {
			c.request(r -> {
				r.method(r.POST());
				r.urlPath("/ws");
				r.headers(h -> {
					h.contentType(h.textXml());
					h.header("SoapAction", "http://com.example.webservice.receiver/webservice/Receiver/ReceiveRequest");
				});
				r.body("<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:web=\"http://com.example.webservice.receiver/webservice\">"
						+ "<soapenv:Header/>" + "<soapenv:Body>" + "<web:ReceiveCommand>" + "<Command_1>"
						+ "<id>1234</id>" + "<text>This is a message</text>" + "</Command_1>" + "</web:ReceiveCommand>"
						+ "</soapenv:Body>" + "</soapenv:Envelope>");
			});
			c.response(r -> {
				r.status(r.OK());
				r.headers(h -> {
					h.header(h.contentType(), "text/xml; charset=utf-8");
				});
				r.body("<SOAP-ENV:Envelope xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\">"
						+ "<SOAP-ENV:Header/>" + "<SOAP-ENV:Body>"
						+ "<ns3:ReceiveResponse xmlns:ns3=\"http://com.example.webservice.receiver/webservice\">"
						+ "<result>1</result>" + "<text>Message</text>"
						+ "<someUuid>5abcf89e-2648-4be1-bd4c-0981c8512784</someUuid>" + "</ns3:ReceiveResponse>"
						+ "</SOAP-ENV:Body>" + "</SOAP-ENV:Envelope>");
				r.bodyMatchers(m -> {
					m.xPath("/SOAP-ENV:Envelope/SOAP-ENV:Body/ns3:ReceiveResponse/result/text()", m.byRegex(r.number()));
					m.xPath("/SOAP-ENV:Envelope/SOAP-ENV:Body/ns3:ReceiveResponse/text/text()", m.byRegex(r.onlyAlphaUnicode()));
					m.xPath("/SOAP-ENV:Envelope/SOAP-ENV:Body/ns3:ReceiveResponse/someUuid/text()", m.byRegex(r.uuid()));
				});
			});
		});
	}

	;

}
// end::class[]
