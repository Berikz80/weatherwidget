package by.isb.weatherwidget.data.mappers
interface Mapper<F, T> {
    fun map(from: F): T
}