package br.com.darp.rest;

import br.com.darp.domain.McpServer;
import br.com.darp.domain.Status;
import br.com.darp.dto.McpServerDTO;
import br.com.darp.exception.DomainException;
import br.com.darp.service.McpService;
import io.smallrye.common.constraint.NotNull;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PATCH;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/api/v1/mcp-servers")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class McpServerResource {

    @Inject
    McpService mcpService;

    @POST
    public Response create(@NotNull McpServerDTO mcpServer) {
        try {
            McpServer createdServer = mcpService.createMcpServer(mcpServer);
            return Response.status(Response.Status.CREATED).entity(createdServer).build();
        } catch (DomainException e) {
            return Response.status(Response.Status.NOT_ACCEPTABLE).entity(e.getMessage()).build();
        }
    }

    @GET
    public Response getMcps(@QueryParam("status") String status) {
        if (status != null && (status.equalsIgnoreCase(Status.ATIVO.name()))) {
            return Response.ok(mcpService.getMcpsPorStatus(status)).build();
        }
        return Response.ok(mcpService.getAllMcpServers()).build();
    }

    @PATCH
    @Path("/{id}")
    public Response alterarStatusMcpServer(@PathParam("id") String id, StatusRequest statusRequest) {
        try {
            mcpService.alterarStaturMcpServer(id, statusRequest.getStatus().trim().toUpperCase(),
                    statusRequest.getAprovador().trim().toUpperCase());
            return Response.ok().build();
        } catch (DomainException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        }
    }

}
