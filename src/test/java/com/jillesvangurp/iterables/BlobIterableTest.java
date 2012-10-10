/**
 * Copyright (c) 2012, Jilles van Gurp
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.jillesvangurp.iterables;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

import org.hamcrest.CoreMatchers;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.xml.sax.SAXException;

@Test
public class BlobIterableTest {

	@DataProvider
	public String[][] input() {
		return new String[][] {
				{ "<i>1</i><i>2</i><i>3</i>", "<i>", "</i>" },
				{ "<list> <i>\n\t1</i>\n<i>2</i><i>3</i><list>", "<i>", "</i>" },
				{ "<i>\n\t1</i>\n<i>2</i><i>3</i><list>", "<i>", "</i>" },
				{ "[[]]]    [[    ]]] [[[]] ", "[[", "]]" } };
	}

	@Test(dataProvider = "input")
	public void shouldIterateOverBlobs(String xml, String begin, String end) {
		ByteArrayInputStream is = new ByteArrayInputStream(xml.getBytes(Charset
				.forName("utf-8")));
		BlobIterable xmlBlobIterable = new BlobIterable(new InputStreamReader(
				is), begin, end);

		int count = 0;
		for (String blob : xmlBlobIterable) {
			assertThat("Should start with", blob.startsWith(begin));
			assertThat("Should end with", blob.endsWith(end));
			count++;
		}
		assertThat(count, CoreMatchers.is(3));
	}

	public void parseWikiPediaUsingBlobIterable() throws SAXException, IOException {
		int count = 0;
		InputStreamReader reader = new InputStreamReader(this.getClass()
				.getResourceAsStream("/wikipediasample.xml"),
				Charset.forName("utf-8"));
		try {
			for (String page : new BlobIterable(new BufferedReader(reader),
					"<page>", "</page>")) {
				assertThat("Should start with", page.startsWith("<page>"));
				assertThat("Should end with", page.endsWith("</page>"));
				count++;
			}
			assertThat(count, is(83));
		} finally {
			reader.close();
		}
	}
}
