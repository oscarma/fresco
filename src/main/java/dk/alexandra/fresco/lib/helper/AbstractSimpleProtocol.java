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
package dk.alexandra.fresco.lib.helper;

import dk.alexandra.fresco.framework.NativeProtocol;
import dk.alexandra.fresco.framework.Protocol;
import dk.alexandra.fresco.framework.ProtocolProducer;
import dk.alexandra.fresco.framework.value.Value;

/**
 * Implements the basic structure of a simple circuit. I.e., a circuit which can
 * be expressed as a single GateProducer initialized at the first call to
 * getNextGates. Essentially, most circuits should be expressable in this way,
 * except perhaps for more advanced circuits such as reactive circuits.
 * 
 * @author psn
 * 
 */
public abstract class AbstractSimpleProtocol implements Protocol {

	private boolean done = false;
	private ProtocolProducer gp = null;
	private Value[] inputs = null;
	private Value[] outputs = null;

	@Override
	public int getNextProtocols(NativeProtocol[] gates, int pos) {
		if (gp == null) {
			gp = initializeGateProducer();
			if (gp == null) {
				done = true;
				return pos;
			}
		}
		if (gp.hasNextProtocols()) {
			pos = gp.getNextProtocols(gates, pos);
			/*
			int end = gp.getNextGates(gates, pos);
			//GateRegister.registerGates(gates, pos, end, this);
			pos = end;
			*/
		} else if (!gp.hasNextProtocols()) {
			done = true;
			gp = null;
		}
		return pos;
	}

	@Override
	public boolean hasNextProtocols() {
		return !done;
	}

	@Override
	public Value[] getInputValues() {
		return inputs;
	}

	@Override
	public Value[] getOutputValues() {
		return outputs;
	}

	/**
	 * Sets the input values of this circuit.
	 * 
	 * @param inputs
	 *            the input values of the circuit.
	 */
	protected void setInputValues(Value[] inputs) {
		
		this.inputs = inputs;
	}

	/**
	 * Sets the output values of this circuit.
	 * 
	 * @param outputs
	 *            the output values of the circuit.
	 */
	protected void setOutputValues(Value[] outputs) {
		this.outputs = outputs;
	}

	/**
	 * Initializes the GateProducer for this circuit.
	 * 
	 * @return the GateProducer for this circuit.
	 */
	protected abstract ProtocolProducer initializeGateProducer();
}
