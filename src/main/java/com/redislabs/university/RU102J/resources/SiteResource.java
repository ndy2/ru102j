package com.redislabs.university.RU102J.resources;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.redislabs.university.RU102J.api.Site;
import com.redislabs.university.RU102J.dao.SiteDao;

@Path("/sites")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SiteResource {

	private final SiteDao siteDao;

	public SiteResource(SiteDao siteDao) {
		this.siteDao = siteDao;
	}

	@GET
	public Response getSites() {
		return Response.ok(siteDao.findAll())
			.header("Access-Control-Allow-Origin", "*")
			.build();
	}

	@GET
	@Path("/{id}")
	public Response getSite(@PathParam("id") Long id) {
		Site site = siteDao.findById(id);
		if (site == null) {
			return Response.noContent().status(404).build();
		}
		return Response.ok(site).header("Access-Control-Allow-Origin", "*").build();
	}
}
