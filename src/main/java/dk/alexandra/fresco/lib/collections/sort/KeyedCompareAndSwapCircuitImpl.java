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
package dk.alexandra.fresco.lib.collections.sort;

import dk.alexandra.fresco.framework.ProtocolProducer;
import dk.alexandra.fresco.framework.value.SBool;
import dk.alexandra.fresco.lib.compare.KeyedCompareAndSwapCircuit;
import dk.alexandra.fresco.lib.helper.AbstractSimpleProtocol;
import dk.alexandra.fresco.lib.helper.builder.BasicLogicBuilder;
import dk.alexandra.fresco.lib.logic.AbstractBinaryFactory;

public class KeyedCompareAndSwapCircuitImpl extends AbstractSimpleProtocol
		implements KeyedCompareAndSwapCircuit {

	private SBool[] leftKey;
	private SBool[] leftValue;
	private SBool[] rightKey;
	private SBool[] rightValue;
	private AbstractBinaryFactory bp;

	/**
	 * Constructs a gate producer for the keyed compare and swap circuit. This
	 * circuit will compare the keys of two key-value pairs and swap the pairs
	 * so that the left pair has the largest key.
	 * 
	 * @param leftKey
	 *            the key of the left pair
	 * @param leftValue
	 *            the value of the left pair
	 * @param rightKey
	 *            the key of the right pair
	 * @param rightValue
	 *            the value of the right pair
	 * @param bp
	 *            a provider of binary circuits
	 */
	public KeyedCompareAndSwapCircuitImpl(SBool[] leftKey, SBool[] leftValue,
			SBool[] rightKey, SBool[] rightValue, AbstractBinaryFactory bp) {
		this.leftKey = leftKey;
		this.leftValue = leftValue;
		this.rightKey = rightKey;
		this.rightValue = rightValue;
		this.bp = bp;
	}

	@Override
	protected ProtocolProducer initializeGateProducer() {
		BasicLogicBuilder blb = new BasicLogicBuilder(bp);
		blb.beginSeqScope();
		blb.beginParScope();
		SBool compRes = blb.greaterThan(leftKey, rightKey);
		SBool[] tmpXORKey = blb.xor(leftKey, rightKey);
		SBool[] tmpXORValue = blb.xor(leftValue, rightValue);
		blb.endCurScope();

		blb.beginParScope();
		blb.condSelectInPlace(leftKey, compRes, leftKey, rightKey);
		blb.condSelectInPlace(leftValue, compRes, leftValue, rightValue);
		blb.endCurScope();

		blb.beginParScope();
		blb.xorInPlace(rightKey, leftKey, tmpXORKey);
		blb.xorInPlace(rightValue, leftValue, tmpXORValue);
		blb.endCurScope();

		blb.endCurScope();
		return blb.getCircuit();
	}
}