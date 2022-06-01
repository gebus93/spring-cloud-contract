package org.springframework.cloud.contract.verifier.builder;

import org.junit.Test;
import org.springframework.cloud.contract.spec.Contract;
import org.springframework.cloud.contract.spec.internal.BodyMatchers;

import java.util.Optional;

import static com.toomuchcoding.jsonassert.JsonAssertion.assertThat;
import static org.junit.Assert.assertEquals;

public class XmlBodyVerificationBuilderTest {

	private static final String xml = "<customer>\r\n" + "      <email>customer@test.com</email>\r\n"
			+ "    </customer>";

	private static final String soapXml = "<SOAP-ENV:Envelope xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\">\n"
			+ "<SOAP-ENV:Header/><SOAP-ENV:Body>\n"
			+ "<ns3:ReceiveResponse xmlns:ns3=\"http://com.example.webservice.receiver/webservice\">\n"
			+ "<result>1</result><text>Message</text>\n"
			+ "<someUuid>5abcf89e-2648-4be1-bd4c-0981c8512784</someUuid></ns3:ReceiveResponse>\n"
			+ "</SOAP-ENV:Body></SOAP-ENV:Envelope>";

	@Test
	public void shouldAddXmlProcessingLines() {
		// Given
		XmlBodyVerificationBuilder builder = new XmlBodyVerificationBuilder(new Contract(), Optional.of(";"));
		BlockBuilder blockBuilder = new BlockBuilder(" ");
		BodyMatchers matchers = new BodyMatchers();
		// When
		builder.addXmlResponseBodyCheck(blockBuilder, xml, matchers, xml, true);
		// Then
		String test = blockBuilder.toString();
		assertThat(test).contains("DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();")
				.contains("builderFactory.setNamespaceAware(true);")
				.contains("DocumentBuilder documentBuilder = builderFactory.newDocumentBuilder();")
				.contains("Document parsedXml = documentBuilder.parse(new InputSource(new StringReader(").contains(xml);
	}

	@Test
	public void shouldGenerateSoapVerificationBlock() {
		// Given
		XmlBodyVerificationBuilder builder = new XmlBodyVerificationBuilder(new Contract(), Optional.of(";"));
		BlockBuilder blockBuilder = new BlockBuilder(" ");
		BodyMatchers matchers = new BodyMatchers();
		// When
		builder.addXmlResponseBodyCheck(blockBuilder, soapXml, matchers, soapXml, true);
		// Then
		String test = blockBuilder.toString();
		String expected = "DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();\n"
				+ "builderFactory.setNamespaceAware(true);\n"
				+ "DocumentBuilder documentBuilder = builderFactory.newDocumentBuilder();\n"
				+ "Document parsedXml = documentBuilder.parse(new InputSource(new StringReader(<SOAP-ENV:Envelope xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\">\n"
				+ "<SOAP-ENV:Header/><SOAP-ENV:Body>\n"
				+ "<ns3:ReceiveResponse xmlns:ns3=\"http://com.example.webservice.receiver/webservice\">\n"
				+ "<result>1</result><text>Message</text>\n"
				+ "<someUuid>5abcf89e-2648-4be1-bd4c-0981c8512784</someUuid></ns3:ReceiveResponse>\n"
				+ "</SOAP-ENV:Body></SOAP-ENV:Envelope>)));\n" + "// and:\n"
				+ "assertThat(valueFromXPath(parsedXml, \"/SOAP-ENV:Envelope/SOAP-ENV:Body/ns3:ReceiveResponse/result/text()\")).isEqualTo(\"1\");\n"
				+ "assertThat(valueFromXPath(parsedXml, \"/SOAP-ENV:Envelope/SOAP-ENV:Body/ns3:ReceiveResponse/text/text()\")).isEqualTo(\"Message\");\n"
				+ "assertThat(valueFromXPath(parsedXml, \"/SOAP-ENV:Envelope/SOAP-ENV:Body/ns3:ReceiveResponse/someUuid/text()\")).isEqualTo(\"5abcf89e-2648-4be1-bd4c-0981c8512784\");\n"
				+ "assertThat(valueFromXPath(parsedXml, \"/SOAP-ENV:Envelope/namespace::SOAP-ENV\")).isEqualTo(\"http://schemas.xmlsoap.org/soap/envelope/\");\n"
				+ "assertThat(valueFromXPath(parsedXml, \"/SOAP-ENV:Envelope/SOAP-ENV:Body/ns3:ReceiveResponse/namespace::ns3\")).isEqualTo(\"http://com.example.webservice.receiver/webservice\");\n";
		assertEquals(expected, test);
	}

}