package kotlinx.jdk.numbers

private fun Int.minimum(minVal: Int): Int = Math.max(this, minVal)
private fun Int.maximum(maxVal: Int): Int = Math.min(this, maxVal)
private fun Int.coerce(minVal: Int, maxVal: Int) = this.minimum(minVal).maximum(maxVal)
private fun Int.coerce(range: IntRange) = this.minimum(range.start).maximum(range.end)