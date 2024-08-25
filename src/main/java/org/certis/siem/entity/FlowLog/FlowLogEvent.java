package org.certis.siem.entity.FlowLog;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FlowLogEvent {

    private String id;

    private long timestamp;
    private String message;


    //
    private int version;
    private String accountId;
    private String interfaceId;
    private String srcAddr;
    private String dstAddr;
    private int srcPort;
    private int dstPort;
    private int protocol;
    private long packets;
    private long bytes;
    private long start;
    private long end;
    private String action;
    private String logStatus;

    //todo. Convert FlowLog
    /*
    public static VPCFlowLog parseLog(String id, long timestamp, String message) {
        String[] tokens = message.split(" ");

        if (tokens.length != 14) {
            throw new IllegalArgumentException("Invalid log message format");
        }

        VPCFlowLog log = VPCFlowLog.builder()
                .id(id)
                .timestamp(timestamp)
                .version(Integer.parseInt(tokens[0]))
                .accountId(tokens[1])
                .interfaceId(tokens[2])
                .srcAddr(tokens[3])
                .dstAddr(tokens[4])
                .srcPort(Integer.parseInt(tokens[5]))
                .dstPort(Integer.parseInt(tokens[6]))
                .protocol(Integer.parseInt(tokens[7]))
                .packets(Long.parseLong(tokens[8]))
                .bytes(Long.parseLong(tokens[9]))
                .start(Long.parseLong(tokens[10]))
                .end(Long.parseLong(tokens[11]))
                .action(tokens[12])
                .logStatus(tokens[13])
                .message(message)
                .build();

        return log;
    }
     */
}
