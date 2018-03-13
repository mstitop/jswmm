/*
 * This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.altervista.growworkinghard.jswmm.dataStructure.runoff;

import org.altervista.growworkinghard.jswmm.runoff.RunoffODE;
import org.apache.commons.math3.ode.FirstOrderDifferentialEquations;
import org.apache.commons.math3.ode.FirstOrderIntegrator;

import java.time.Instant;

public class SWMM5RunoffSetup implements RunoffSetup {

    private String areaName;

    private Instant initialTime;

    private Instant totalTime;

    private Long runoffStepSize;

    private FirstOrderIntegrator firstOrderIntegrator;

    private FirstOrderDifferentialEquations ode = new RunoffODE(0.0, 0.0);

    public SWMM5RunoffSetup(Instant initialTime, Instant totalTime, Long runoffStepSize,
                            FirstOrderIntegrator firstOrderIntegrator) {
        this.initialTime = initialTime;
        this.totalTime = totalTime;
        this.runoffStepSize = runoffStepSize;
        this.firstOrderIntegrator = firstOrderIntegrator;
    }

    @Override
    public Instant getInitialTime() {
        return initialTime;
    }

    @Override
    public Instant getTotalTime() {
        return totalTime;
    }

    @Override
    public Long getRunoffStepSize() {
        return runoffStepSize;
    }

    @Override
    public FirstOrderIntegrator getFirstOrderIntegrator() {
        return firstOrderIntegrator;
    }

    @Override
    public FirstOrderDifferentialEquations getOde() {
        return ode;
    }

    @Override
    public void setOde(Double rainfall, Double depthFactor) {
        this.ode = new RunoffODE(rainfall, depthFactor);
    }

    @Override
    public String getAreaName() {
        return areaName;
    }
}
