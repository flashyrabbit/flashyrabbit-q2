/*
Copyright (C) 1997-2001 Id Software, Inc.

This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either version 2
of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  

See the GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.

*/
/* Modifications
   Copyright 2003-2004 Bytonic Software
   Copyright 2010 Google Inc.
*/
package jake2.render;


import jake2.game.cplane_t;
import jake2.qcommon.Defines;
import jake2.qcommon.qfiles;
import jake2.util.Lib;
import jake2.util.Math3D;

import java.util.Arrays;

public class model_t implements Cloneable {
	
	public String name = "";

	public int registration_sequence;

	// was enum modtype_t
	public int type;
	public int numframes;

	public int flags;

	//
	// volume occupied by the model graphics
	//		
	public float[] mins = { 0, 0, 0 }, maxs = { 0, 0, 0 };
	public float radius;

	//
	// solid volume for clipping 
	//
	public boolean clipbox;
	public float clipmins[] = { 0, 0, 0 }, clipmaxs[] = { 0, 0, 0 };

	//
	// brush model
	//
	public int firstmodelsurface, nummodelsurfaces;
	public int lightmap; // only for submodels

	public int numsubmodels;
	public mmodel_t submodels[];

	public int numplanes;
	public cplane_t planes[];

	public int numleafs; // number of visible leafs, not counting 0
	public mleaf_t leafs[];

	public int numvertexes;
	public mvertex_t vertexes[];

	public int numedges;
	public medge_t edges[];

	public int numnodes;
	public int firstnode;
	public mnode_t nodes[];

	public int numtexinfo;
	public mtexinfo_t texinfo[];

	public int numsurfaces;
	public msurface_t surfaces[];

	public int numsurfedges;
	public int surfedges[];

	public int nummarksurfaces;
	public msurface_t marksurfaces[];

	public qfiles.dvis_t vis;

	public byte lightdata[];

	// for alias models and skins
	// was image_t *skins[]; (array of pointers)
	public image_t skins[] = new image_t[Defines.MAX_MD2SKINS];

	public int extradatasize;

	// or whatever
	public Object extradata;
	
	public void clear() {
		name = "";
		registration_sequence = 0;

		// was enum modtype_t
		type = 0;
		numframes = 0;
		flags = 0;

		//
		// volume occupied by the model graphics
		//		
		Math3D.VectorClear(mins);
		Math3D.VectorClear(maxs);
		radius = 0;

		//
		// solid volume for clipping 
		//
		clipbox = false;
		Math3D.VectorClear(clipmins);
		Math3D.VectorClear(clipmaxs);

		//
		// brush model
		//
		firstmodelsurface = nummodelsurfaces = 0;
		lightmap = 0; // only for submodels

		numsubmodels = 0;
		submodels = null;

		numplanes = 0;
		planes = null;

		numleafs = 0; // number of visible leafs, not counting 0
		leafs = null;

		numvertexes = 0;
		vertexes = null;

		numedges = 0;
		edges = null;

		numnodes = 0;
		firstnode = 0;
		nodes = null;

		numtexinfo = 0;
		texinfo = null;

		numsurfaces = 0;
		surfaces = null;

		numsurfedges = 0;
		surfedges = null;

		nummarksurfaces = 0;
		marksurfaces = null;

		vis = null;

		lightdata = null;

		// for alias models and skins
		// was image_t *skins[]; (array of pointers)
		Arrays.fill(skins, null);

		extradatasize = 0;
		// or whatever
		extradata = null;
	}

	// TODO replace with set(model_t from)
	public model_t copy() {
		model_t theClone;
    theClone = dup();
		theClone.mins = Lib.clone(this.mins);
		theClone.maxs = Lib.clone(this.maxs);
		theClone.clipmins = Lib.clone(this.clipmins);
		theClone.clipmaxs = Lib.clone(this.clipmaxs);
		return theClone;
	}

	public model_t dup() {
	  model_t clone = new model_t();

	  clone.name = name;
	  clone.registration_sequence = registration_sequence;
	  clone.type = type;
	  clone.numframes = numframes;
	  clone.flags = flags;
	  clone.mins = mins;
	  clone.maxs = maxs;
	  clone.radius = radius;
	  clone.clipbox = clipbox;
	  clone.clipmins = clipmins;
	  clone.clipmaxs = clipmaxs;
	  clone.firstmodelsurface = firstmodelsurface;
	  clone.nummodelsurfaces = nummodelsurfaces;
	  clone.lightmap = lightmap;
	  clone.numsubmodels = numsubmodels;
	  clone.submodels = submodels;
	  clone.numplanes = numplanes;
	  clone.planes = planes;
	  clone.numleafs = numleafs;
	  clone.leafs = leafs;
	  clone.numvertexes = numvertexes;
	  clone.vertexes = vertexes;
	  clone.numedges = numedges;
	  clone.edges = edges;
	  clone.numnodes = numnodes;
	  clone.firstnode = firstnode;
	  clone.nodes = nodes;
	  clone.numtexinfo = numtexinfo;
	  clone.texinfo = texinfo;
	  clone.numsurfaces = numsurfaces;
	  clone.surfaces = surfaces;
	  clone.numsurfedges = numsurfedges;
	  clone.surfedges = surfedges;
	  clone.nummarksurfaces = nummarksurfaces;
	  clone.marksurfaces = marksurfaces;
	  clone.vis = vis;
	  clone.lightdata = lightdata;
	  clone.skins = skins;
	  clone.extradatasize = extradatasize;
	  clone.extradata = extradata;

	  return clone;
	}
}
