import scala.annotation.StaticAnnotation
import scala.language.experimental.macros
import scala.language.postfixOps
import scala.reflect.api.Trees
import scala.reflect.macros.whitebox

object ToStringObfuscateMacro {

  def obfuscateValue(value: String): String = "*" * value.length

  def impl(c: whitebox.Context)(annottees: c.Expr[Any]*): c.Expr[Any] = {
    import c.universe._

    def extractAnnotationParameters(tree: Tree) = tree match {
      case q"new $name( ..$params )" => params
      case _ => throw new Exception("ToStringObfuscate annotation must be have at least one parameter.")
    }

    def extractCaseClassesParts(classDecl: ClassDef) = classDecl match {
      case q"case class $className(..$fields) extends ..$parents { ..$body }" =>
        (className, fields, parents, body)
    }

    def replaceCaseClassSensitiveValues(tree: Trees#Tree) = tree match {
      case Literal(Constant(field: String)) =>
        q"""
            ${TermName(field)} = ToStringObfuscateMacro.obfuscateValue(this.${TermName(field)})
          """
      case _ => c.abort(c.enclosingPosition, s"[replaceCaseClassSensitiveValues] Match error with $tree")
    }

    val sensitiveFields = extractAnnotationParameters(c.prefix.tree)

    val fieldReplacements = sensitiveFields.map(f => replaceCaseClassSensitiveValues(f))

    def extractNewToString(sensitiveFields: List[Trees#Tree]) =
      q"""
         override def toString: ${typeOf[String]} = {
          scala.runtime.ScalaRunTime._toString(this.copy(..$fieldReplacements))
         }
      """

    def modifiedDeclaration(classDecl: ClassDef) = {
      val (className, fields, parents, body) = extractCaseClassesParts(classDecl)
      val newToString = extractNewToString(sensitiveFields.toList)

      val params = fields.asInstanceOf[List[ValDef]] map { p => p.duplicate }

      c.Expr[Any](
        q"""
        case class $className ( ..$params ) extends ..$parents {
          $newToString
          ..$body
        }
      """
      )
    }

    annottees map (_.tree) toList match {
      case (classDecl: ClassDef) :: Nil => modifiedDeclaration(classDecl)
      case _ => c.abort(c.enclosingPosition, "Invalid annottee")
    }
  }
}

class ToStringObfuscate(fieldsToObfuscate: String*) extends StaticAnnotation {
  def macroTransform(annottees: Any*): Any = macro ToStringObfuscateMacro.impl
}
