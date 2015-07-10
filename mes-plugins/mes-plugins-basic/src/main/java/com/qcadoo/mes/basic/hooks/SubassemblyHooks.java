/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.3
 * <p>
 * This file is part of Qcadoo.
 * <p>
 * Qcadoo is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation; either version 3 of the License,
 * or (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 * <p>
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 * ***************************************************************************
 */
package com.qcadoo.mes.basic.hooks;

import com.qcadoo.mes.basic.constants.SubassemblyFields;
import com.qcadoo.mes.basic.constants.WorkstationTypeFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchCriterion;
import com.qcadoo.model.api.search.SearchRestrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SubassemblyHooks {

    private static final String GL_EKSTR = "GL.EKSTR";

    @Autowired
    private DataDefinitionService dataDefinitionService;

    public void validateSubassembly(final DataDefinition subassemblyDD, final Entity subassembly) {
        validateRequiredSubassemblyType(subassemblyDD, subassembly);
        validateUniquenessOfWorkstationAndType(subassemblyDD, subassembly);
    }

    private void validateUniquenessOfWorkstationAndType(DataDefinition subassemblyDD, Entity subassembly) {
        SearchCriterion criterionWorkstation = SearchRestrictions.belongsTo(SubassemblyFields.WORKSTATION, subassembly.getBelongsToField(SubassemblyFields.WORKSTATION));
        SearchCriterion criterionType = SearchRestrictions.eq(SubassemblyFields.TYPE, subassembly.getStringField(SubassemblyFields.TYPE));
        SearchCriterion criterionId = subassembly.getId() == null ? null : SearchRestrictions.idNe(subassembly.getId());

        Long count = criterionId == null ? subassemblyDD.count(SearchRestrictions.and(criterionWorkstation, criterionType)) : subassemblyDD.count(SearchRestrictions.and(criterionWorkstation, criterionType, criterionId));
        if (count > 0) {
            subassembly.addGlobalError("basic.validate.global.error.uniquenessOfWorkstationAndType", true);
        }
    }

    private void validateRequiredSubassemblyType(DataDefinition subassemblyDD, Entity subassembly) {
        String workstationTypeName = subassembly.getBelongsToField(SubassemblyFields.WORKSTATION_TYPE).getStringField(WorkstationTypeFields.NUMBER);

        if (GL_EKSTR.equals(workstationTypeName)) {
            String subassemblyType = subassembly.getStringField(SubassemblyFields.TYPE);
            if (subassemblyType == null) {
                subassembly.addError(subassemblyDD.getField(SubassemblyFields.TYPE), "basic.subassembly.type.requiredWithGlEkstr");
            }
        }
    }

}
