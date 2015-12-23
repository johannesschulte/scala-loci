package retier
package impl

import network.ConnectionRequestor

private final case class MultipleRemoteConnectionImpl[
  R <: Peer: PeerTypeTag](system: System)
    extends MultipleRemoteConnectionImplBase[R] {
  val remoteJoined = system.remoteJoined[R]
  val remoteLeft = system.remoteLeft[R]
  def remotes = system.remotes[R]
  def request(requestor: ConnectionRequestor) =
    system.requestRemoteConnection(requestor)
}

private final case class OptionalRemoteConnectionImpl[
  R <: Peer: PeerTypeTag](system: System)
    extends OptionalRemoteConnectionImplBase[R] {
  val remoteJoined = system.remoteJoined[R]
  val remoteLeft = system.remoteLeft[R]
  def remote = system.optionalRemote[R]
  def request(requestor: ConnectionRequestor) =
    system.requestRemoteConnection(requestor)
}

private final case class SingleRemoteConnectionImpl[
  R <: Peer: PeerTypeTag](system: System)
    extends SingleRemoteConnectionImplBase[R] {
  val remoteJoined = system.remoteJoined[R]
  val remoteLeft = system.remoteLeft[R]
  def remote = system.singleRemote[R]
  def request(requestor: ConnectionRequestor) =
    system.requestRemoteConnection(requestor)
}
