package cc.factorie.application
import scala.reflect.Manifest
import cc.factorie.er._

/** Variables and factors for independent classification of feature vectors with String-valued features. */
object FeatureVectorClassification {
  
  abstract class Instance[L<:Label[This,L],This<:Instance[L,This]](val name:String, labelString:String) extends BinaryVectorVariable[String] /*with GetterType[This]*/ {
    this: This =>
    type VariableType <: Instance[L,This]
    class DomainInSubclasses
    type GetterType = InstanceGetter[L,This]
    class GetterClass extends InstanceGetter[L,This]
    def newGetter = new InstanceGetter[L,This]
    def newLabel(labelString:String): L
    val label: L = newLabel(labelString)
  }
  
  abstract class Label[I<:Instance[This,I],This<:Label[I,This]](labelString:String, val instance:I) extends LabelVariable(labelString) /*with GetterType[This]*/ {
  	type GetterType = LabelGetter[I,This];
  	class GetterClass extends LabelGetter[I,This]
    type VariableType <: Label[I,This]
    class DomainInSubclasses
  }
  

  class InstanceGetter[L<:Label[ThisInstance,L],ThisInstance<:Instance[L,ThisInstance]] extends Getter[ThisInstance] {
    def newLabelGetter = new LabelGetter[ThisInstance,L]
    def label = initOneToOne(newLabelGetter, instance => instance.label, (label:L) => label.instance)
  }
  
  class LabelGetter[I<:Instance[ThisLabel,I],ThisLabel<:Label[I,ThisLabel]] extends Getter[ThisLabel] {
    def newInstanceGetter = new InstanceGetter[ThisLabel,I]
    def instance = initOneToOne[I,InstanceGetter[ThisLabel,I]](newInstanceGetter, label => label.instance, (instance:I) => instance.label)
  }
  
  
  /**Bias term just on labels */
  class LabelTemplate[L<:Label[_,L]](implicit lm:Manifest[L]) extends TemplateWithDotStatistics1[L]()(lm) 

  /**Factor between label and observed instance vector */
  class LabelInstanceTemplate[L<:Label[I,L],I<:Instance[L,I]](implicit lm:Manifest[L],im:Manifest[I]) extends TemplateWithDotStatistics2[L,I]()(lm,im) {
    def unroll1(label: L) = Factor(label,label.instance)
    def unroll2(instance: I) = throw new Error("Instance BinaryVectorVariable shouldn't change")
  }

  /**Factor between label and observed instance vector */
  class SparseLabelInstanceTemplate[L<:Label[I,L],I<:Instance[L,I]](implicit lm:Manifest[L],im:Manifest[I]) extends TemplateWithDotStatistics2[L,I]()(lm,im) with SparseWeights {
    def unroll1(label: L) = Factor(label,label.instance)
    def unroll2(instance: I) = throw new Error("Instance BinaryVectorVariable shouldn't change")
  }
  
  def newModel[L<:Label[I,L],I<:Instance[L,I]](implicit lm:Manifest[L],im:Manifest[I]) =
    new Model(
      new LabelTemplate[L],
      new LabelInstanceTemplate[L,I]
    )

  def newObjective[L<:Label[I,L],I<:Instance[L,I]](implicit lm:Manifest[L]) = new TrueLabelTemplate[L]()(lm)

}