class Day16(path: String = "day16/input") {
    private val inputData: List<String> = path.fromResource().readLines()


    fun compute(): Int {
        println(hexToBin(inputData[0]))
        return PacketParser(hexToBin(inputData[0])).count()
    }

    fun compute2(): Int {
        return 0
    }

    companion object {
        fun hexToBin(input: String): String {
            return input.toCharArray().map { hexCharToBinary(it.toString()) }.joinToString("")
        }

        fun hexCharToBinary(hex: String): String {
            val i = hex.toInt(16)
            return Integer.toBinaryString(i).padStart(4, '0')
        }
    }


    open class Packet(open val version: Int, open val typeId: Int, open val length: Int)
    data class LiteralPacket(
        override val version: Int,
        override val typeId: Int,
        override val length: Int,
        val value: String
    ) :
        Packet(version, typeId, length)

    data class OperatorPacket(
        override val version: Int,
        override val typeId: Int,
        override val length: Int,
        val subPacket: List<Packet>
    ) : Packet(version, typeId, length)

    data class PacketParser(val binaryMessage: String) {
        fun String.binToInt() = this.toInt(2)
        fun parse(): Packet {
            val version: Int = packetVesion()
            val typeId: Int = typeId()
            val packet: Packet = if (typeId == 4) {
                parseLiteralPacket(version, typeId, binaryMessage)
            } else {
                parseOperatorPacket(version, typeId, binaryMessage)
            }
            return packet
        }

        fun count(): Int {

            val packet = parse()
            println("${packet.version} ${packet.typeId}")
            return 0
        }

        private fun parseLiteralPacket(version: Int, typeId: Int, binaryMessage: String): Packet {
            val packets = binaryMessage.substring(6)
            val chunked = packets.chunked(5)
            val count = chunked.takeWhile { it[0] != '0' }.size + 1
            val value = chunked.subList(0, count).joinToString("") { it.substring(1) }.binToInt().toString()
            return LiteralPacket(version, typeId, (count * 5) + 6, value)
        }

        private fun parseOperatorPacket(version: Int, typeId: Int, binaryMessage: String): Packet {
            val packetsCount = binaryMessage[6] == '1'
            val messageLengthBitCount = if (packetsCount) 11 else 15
            val headerPlusOperatorLength = 8 + messageLengthBitCount - 1
            val substring = binaryMessage.substring(8, headerPlusOperatorLength)
            var packetsSize = substring.binToInt()
            println(packetsSize)
            val subPacketsBinary = binaryMessage.substring(headerPlusOperatorLength)
            if (!packetsCount) {
                val subPacket = parseSubPackets(subPacketsBinary, packetsSize){it.length}
                return OperatorPacket(
                    version,
                    typeId,
                    8 + messageLengthBitCount + subPacket.sumOf { it.length },
                    subPacket
                )
            }else{
                val subPacket = parseSubPackets(subPacketsBinary, packetsSize){1}
                return OperatorPacket(
                    version,
                    typeId,
                    8 + messageLengthBitCount + subPacket.sumOf { it.length },
                    subPacket
                )
            }
            TODO()
        }

        private fun parseSubPackets(
            binaryMessage: String,
            initialPacketsSize: Int,
            packetSizeDec:(p:Packet)->Int
        ): MutableList<Packet> {
            var packetsSize = initialPacketsSize
            var subBinary = binaryMessage
            val subPacket = mutableListOf<Packet>()
            while (packetsSize > 0) {
                val packet = PacketParser(
                    subBinary
                ).parse()
                subBinary = subBinary.substring(packet.length)
                packetsSize -= packetSizeDec(packet)
                subPacket.add(packet)
            }
            return subPacket
        }


        private fun typeId(): Int {
            val substring = binaryMessage.substring(3, 6)
            return substring.binToInt()
        }

        private fun packetVesion(): Int {
            val substring = binaryMessage.substring(0, 3)
            return substring.binToInt()
        }


    }
}
