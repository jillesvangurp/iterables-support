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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * This class iterates over string blobs in a Reader that are clearly marked
 * with some begin and end token. This is useful for processing large files of
 * e.g. xml, json, or some other structured data in a streaming fashion and allows you to process the
 * blobs one by one instead of parsing the whole file all at once, which over a certain size
 * might be very challenging.
 *
 * Basically this class is an Iterable<String>, which means you can simply use a
 * for each loop to iterate over the content.
 *
 * Note. make sure to close the reader after iterating. This class does not attempt to close the reader.
 */
public class BlobIterable implements Iterable<String> {

	private final Reader r;
	private final String openTag;
	private final String closeTag;

	/**
	 * @param r reader, DO NOT forget to close the reader!
	 * @param openTag
	 * @param closeTag
	 */
	public BlobIterable(Reader r, String openTag, String closeTag) {
		this.r = r;
		this.openTag = openTag;
		this.closeTag = closeTag;
	}

	@Override
	public Iterator<String> iterator() {
		final BufferedReader br = new BufferedReader(r);

		return new BlobIterator(br);
	}

	private final class BlobIterator implements Iterator<String> {
		private final BufferedReader br;
		StringBuilder current = new StringBuilder();

		String next;

		private BlobIterator(BufferedReader br) {
			this.br = br;
			readNext();
		}

		private void readNext() {
			current = new StringBuilder();
			next = null;
			try {
				int c;
				if(next == null) {
					while(next == null && (c = br.read()) != -1) {
						if(openTag.charAt(0) == c) {
							current.append((char)c);
							int o=1;
							while(o<openTag.length() && (c=br.read()) != -1) {
								current.append((char)c);
								o++;
							}
							if(openTag.equals(current.toString())) {

								while(!fastEndsWith(current, closeTag) && (c=br.read()) != -1) {
									current.append((char)c);
								}
								if(fastEndsWith(current, closeTag)) {
									next = current.toString();
									return;
								}
							}
							current = new StringBuilder();
						}
					}
				}
			} catch (IOException e) {
				throw new IllegalStateException("cannot read from stream",e);
			}
		}

		private boolean fastEndsWith(CharSequence buf, String postFix) {
			// String.endsWith is very slow and creating extra String objects
			// every time we want to check the StringBuilder content is
			// inefficient
			if(buf.length()<postFix.length()) {
				return false;
			} else {
				boolean match = true;
				for(int i=1;i<=postFix.length();i++) {
					match = match && buf.charAt(buf.length()-i) == postFix.charAt(postFix.length()-i);
					if(!match) {
						return false;
					}
				}
				return match;
			}
		}

		@Override
		public boolean hasNext() {
			return next != null;
		}

		@Override
		public String next() {
			String result = next;
			if(next != null) {
				readNext();
				return result;
			} else {
				throw new NoSuchElementException();
			}
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException("remove is not supported");
		}
	}
}
