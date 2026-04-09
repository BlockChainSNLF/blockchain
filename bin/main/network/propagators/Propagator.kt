package network.propagators

interface Propagator<T> {
    fun propagate(item: T, peers: List<String>)
}