/* Copyright (C) 2008-2010 University of Massachusetts Amherst,
   Department of Computer Science.
   This file is part of "FACTORIE" (Factor graphs, Imperative, Extensible)
   http://factorie.cs.umass.edu, http://code.google.com/p/factorie/
   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at
    http://www.apache.org/licenses/LICENSE-2.0
   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License. */

package cc.factorie
import cc.factorie.la._


/** A DiscreteDomain for holding a singleton Tensor holding a single real (Double) value. */
object RealDiscreteDomain extends DiscreteDomain(1)
trait RealDomain extends DiscreteDimensionTensorDomain with Domain[RealValue] {
  def dimensionDomain = RealDiscreteDomain
}
object RealDomain extends RealDomain

/** A Tensor holding a single real (Double) value. */
// In Scala 2.10 make this an implicit class.
final class RealValue(val singleValue:Double) extends Tensor1 with SingletonTensor /*with scala.math.Numeric[RealValue]*/ {
  def domain = RealDomain
  @inline final def dim1 = 1
  @inline final def singleIndex = 0
  def activeDomain = new cc.factorie.util.SingletonIntSeq(singleIndex)
  @inline final def doubleValue: Double = singleValue // TODO Consider swapping singleIndex <-> intValue
  @inline final def intValue: Int = singleValue.toInt // TODO Consider swapping singleIndex <-> intValue
  override def toString: String = singleValue.toString
  def +(r:RealValue) = new RealValue(r.doubleValue + singleValue)
  def -(r:RealValue) = new RealValue(r.doubleValue - singleValue)
  def *(r:RealValue) = new RealValue(r.doubleValue * singleValue)
  def /(r:RealValue) = new RealValue(r.doubleValue / singleValue)
  def unary_- = new RealValue(-singleValue)
//  type T = RealValue
//  def plus(x: T, y: T): T = x + y
//  def minus(x: T, y: T): T = x - y
//  def times(x: T, y: T): T = x * y
//  def negate(x: T): T = new RealValue(- x.doubleValue)
//  def fromInt(x: Int): T = new RealValue(x.toDouble)
//  def toInt(x: T): Int = singleValue.toInt
//  def toLong(x: T): Long = singleValue.toLong
//  def toFloat(x: T): Float = singleValue.toFloat
//  def toDouble(x: T): Double = singleValue
}

/** A variable with Tensor value which holds a single real (Double) value.
    Unlike a DoubleValue, these can be used in DotFamilyWithStatistics because its value is a Tensor. */
trait RealVar extends DiscreteDimensionTensorVar with ScalarVar with VarWithValue[RealValue] {
  def doubleValue: Double
  def domain = RealDomain
  @inline final def value: RealValue = new RealValue(doubleValue)
  @inline final def tensor: RealValue = value
  def intValue: Int = doubleValue.toInt
  override def toString = printName + "(" + doubleValue.toString + ")"
}

trait MutableRealVar extends RealVar with MutableDoubleScalarVar with MutableIntScalarVar with MutableVar[RealValue]

/** A Variable with a mutable real (double) value. */
class RealVariable(initialValue: Double) extends MutableRealVar {
  def this() = this(0.0)
  def this(rv:RealValue) = this(rv.singleValue)
  private var _value: Double = initialValue
  @inline final def doubleValue = _value
  def :=(x:Double) = _value = x
  def +=(x:Double) = _value += x
  def -=(x:Double) = _value -= x
  def *=(x:Double) = _value *= x
  def /=(x:Double) = _value /= x
  def set(newValue: Double)(implicit d: DiffList): Unit = if (newValue != _value) {
    if (d ne null) d += new RealDiff(_value, newValue)
    _value = newValue
  }
  final def set(newValue: RealValue)(implicit d: DiffList): Unit = set(newValue.doubleValue)
  final def set(newValue:Int)(implicit d:DiffList): Unit = set(newValue.toDouble)
  case class RealDiff(oldValue: Double, newValue: Double) extends Diff {
    def variable: RealVariable = RealVariable.this
    def redo = _value = newValue
    def undo = _value = oldValue
  }
}
