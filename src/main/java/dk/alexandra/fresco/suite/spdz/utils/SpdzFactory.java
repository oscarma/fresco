/*******************************************************************************
 * Copyright (c) 2015 FRESCO (http://github.com/aicis/fresco).
 *
 * This file is part of the FRESCO project.
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
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.  IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 *
 * FRESCO uses SCAPI - http://crypto.biu.ac.il/SCAPI, Crypto++, Miracl, NTL,
 * and Bouncy Castle. Please see these projects for any further licensing issues.
 *******************************************************************************/
package dk.alexandra.fresco.suite.spdz.utils;

import java.math.BigInteger;
import java.security.SecureRandom;

import dk.alexandra.fresco.framework.value.KnownSIntProtocol;
import dk.alexandra.fresco.framework.value.OInt;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.field.integer.AddProtocol;
import dk.alexandra.fresco.lib.field.integer.BasicNumericFactory;
import dk.alexandra.fresco.lib.field.integer.CloseIntProtocol;
import dk.alexandra.fresco.lib.field.integer.MultProtocol;
import dk.alexandra.fresco.lib.field.integer.OpenIntProtocol;
import dk.alexandra.fresco.lib.field.integer.SubtractCircuit;
import dk.alexandra.fresco.lib.math.PreprocessedNumericBitFactory;
import dk.alexandra.fresco.lib.math.exp.ExpFromOIntFactory;
import dk.alexandra.fresco.lib.math.exp.PreprocessedExpPipeFactory;
import dk.alexandra.fresco.lib.math.inv.LocalInversionCircuit;
import dk.alexandra.fresco.lib.math.inv.LocalInversionFactory;
import dk.alexandra.fresco.suite.spdz.datatypes.SpdzElement;
import dk.alexandra.fresco.suite.spdz.datatypes.SpdzOInt;
import dk.alexandra.fresco.suite.spdz.datatypes.SpdzSInt;
import dk.alexandra.fresco.suite.spdz.gates.SpdzAddProtocol;
import dk.alexandra.fresco.suite.spdz.gates.SpdzInputProtocol;
import dk.alexandra.fresco.suite.spdz.gates.SpdzKnownSIntProtocol;
import dk.alexandra.fresco.suite.spdz.gates.SpdzLocalInversionProtocol;
import dk.alexandra.fresco.suite.spdz.gates.SpdzMultProtocol;
import dk.alexandra.fresco.suite.spdz.gates.SpdzOutputProtocol;
import dk.alexandra.fresco.suite.spdz.gates.SpdzOutputToAllProtocol;
import dk.alexandra.fresco.suite.spdz.gates.SpdzSubtractProtocol;
import dk.alexandra.fresco.suite.spdz.storage.SpdzStorage;

public class SpdzFactory implements BasicNumericFactory, PreprocessedNumericBitFactory,
		PreprocessedExpPipeFactory, ExpFromOIntFactory, LocalInversionFactory {

	private int maxBitLength;
	private SpdzStorage storage;
	private SecureRandom rand;
	private int pID;

	/**
	 * 
	 * @param storage
	 * @param pID
	 * @param maxBitLength
	 *            The maximum length in bits that the numbers in the
	 *            application will have. If you have greater knowledge of your
	 *            application, you can create several providers, each with a
	 *            different maxBitLength to increase performance.
	 */
	//TODO: Make SpdzProvider decoupled from the storage.
	public SpdzFactory(SpdzStorage storage, int pID, int maxBitLength) {
		this.maxBitLength = maxBitLength;
		rand = new SecureRandom();
		this.storage = storage;
		this.pID = pID;
	}

	@Override
	public SInt getSInt() {
		return new SpdzSInt();
	}

	/**
	 * Careful - This creates a publicly known integer which is secret shared.
	 */

	public KnownSIntProtocol getSInt(int i, SInt si) {
		return new SpdzKnownSIntProtocol(i, si);
	}

	/**
	 * Careful - This creates a publicly known integer which is secret shared.
	 */
	public KnownSIntProtocol getSInt(BigInteger value, SInt sValue) {
		return new SpdzKnownSIntProtocol(value, sValue);
	}

	/**
	 * Careful - This creates a publicly known integer which is secret shared.
	 * This is (approximately) the square root of the maximum representable
	 * value. We set "max" to this value as we may want to multiply the "max"
	 * value with an other number, and still not get overflow.
	 */
	@Override
	public SInt getSqrtOfMaxValue() {
		SpdzElement elm;
		BigInteger two = BigInteger.valueOf(2);
		BigInteger max = Util.getModulus().subtract(BigInteger.ONE).divide(two);
		int bitlength = max.bitLength();
		BigInteger approxMaxSqrt = two.pow(bitlength / 2);

		if (pID == 1) {
			elm = new SpdzElement(approxMaxSqrt,
					approxMaxSqrt.multiply(this.storage.getSSK()));
		} else {
			elm = new SpdzElement(BigInteger.ZERO,
					approxMaxSqrt.multiply(this.storage.getSSK()));
		}
		return new SpdzSInt(elm);
	}

	@Override
	public SInt getRandomSecretSharedBit() {
		return this.storage.getSupplier().getNextBit();
	}

	@Override
	public SInt[] getExponentiationPipe() {
		return this.storage.getSupplier().getNextExpPipe();
	}

	@Override
	public OInt[] getExpFromOInt(OInt value, int maxBitSize) {
		BigInteger[] res = Util.getClearExpPipe(value.getValue());
		OInt[] expPipe = new OInt[res.length];
		for (int i = 0; i < res.length; i++) {
			expPipe[i] = new SpdzOInt(res[i]);
		}
		return expPipe;
	}

	@Override
	public SInt getRandomSInt() {
		return new SpdzSInt(storage.getSupplier().getNextTriple().getA());
	}

	@Override
	public OInt getOInt() {
		return new SpdzOInt();
	}

	@Override
	public OInt getOInt(BigInteger i) {
		return new SpdzOInt(i.mod(Util.getModulus()));
	}

	@Override
	public OInt getRandomOInt() {
		return new SpdzOInt(new BigInteger(
				Util.getModulus().toByteArray().length, rand));
	}

	@Override
	public AddProtocol getAddProtocol(SInt a, SInt b, SInt out) {
		return new SpdzAddProtocol(a, b, out);
	}

	@Override
	public AddProtocol getAddProtocol(SInt a, OInt b, SInt out) {
		return new SpdzAddProtocol(a, b, out, this);
	}

	@Override
	public SubtractCircuit getSubtractCircuit(SInt a, SInt b, SInt out) {
		return new SpdzSubtractProtocol(a, b, out, this);
	}

	@Override
	public SubtractCircuit getSubtractCircuit(OInt a, SInt b, SInt out) {
		return new SpdzSubtractProtocol(a, b, out, this);
	}

	@Override
	public MultProtocol getMultCircuit(SInt a, SInt b, SInt out) {
		return new SpdzMultProtocol(a, b, out);
	}

	@Override
	public MultProtocol getMultCircuit(OInt a, SInt b, SInt out) {
		return new SpdzMultProtocol(a, b, out);
	}

	@Override
	public int getMaxBitLength() {
		return this.maxBitLength;
	}

	/****************************************
	 * Native gates or circuits to Spdz *
	 ****************************************/

	@Override
	public LocalInversionCircuit getLocalInversionCircuit(OInt in, OInt out) {
		return new SpdzLocalInversionProtocol(in, out);
	}

	@Override
	@Deprecated
	public SInt getSInt(int i) {

		BigInteger b = BigInteger.valueOf(i).mod(Util.getModulus());
		SpdzElement elm;
		if (pID == 1) {
			elm = new SpdzElement(b, b.multiply(this.storage.getSSK()));
		} else {
			elm = new SpdzElement(BigInteger.ZERO, b.multiply(this.storage
					.getSSK()));
		}
		return new SpdzSInt(elm);
	}

	@Override
	@Deprecated
	public SInt getSInt(BigInteger b) {
		b = b.mod(Util.getModulus());
		SpdzElement elm;
		if (pID == 1) {
			elm = new SpdzElement(b, b.multiply(this.storage.getSSK()));
		} else {
			elm = new SpdzElement(BigInteger.ZERO, b.multiply(this.storage
					.getSSK()));
		}
		return new SpdzSInt(elm);
	}

	/****************************************
	 * IO Provider Stuff *
	 ****************************************/

	public CloseIntProtocol getCloseProtocol(BigInteger open,
			SInt closed, int targetID) {
		return new SpdzInputProtocol(open, closed, targetID);
	}


	@Override
	public CloseIntProtocol getCloseProtocol(int source, OInt open, SInt closed) {
		return new SpdzInputProtocol(open, closed, source);
	}

	@Override
	public OpenIntProtocol getOpenProtocol(int target, SInt closed, OInt open) {
		return new SpdzOutputProtocol(closed, open, target);
	}

	@Override
	public OpenIntProtocol getOpenProtocol(SInt closed, OInt open) {
		return new SpdzOutputToAllProtocol(closed, open);
	}

}
