package excelreads

class SeqProduct(values: Seq[Any]) extends Product {
  override def canEqual(that: Any) =
    true
  override def productArity =
    values.length
  override def productElement(n: Int) =
    values.apply(n)
}
