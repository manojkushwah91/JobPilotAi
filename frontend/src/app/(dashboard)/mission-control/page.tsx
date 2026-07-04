"use client";

import { useState, useEffect } from "react";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { Input } from "@/components/ui/input";
import { Textarea } from "@/components/ui/textarea";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";

interface Mission {
  id: string;
  title: string;
  targetRole: string;
  targetLocation: string;
  salaryMin: number;
  salaryMax: number;
  status: string;
  totalJobsFound: number;
  totalApplicationsSubmitted: number;
  totalRejected: number;
  totalPending: number;
  dailyApplicationLimit: number;
  deadlineAt: string;
  createdAt: string;
}

interface AgentStatus {
  status: string;
  version: string;
  activeMissions: number;
}

export default function MissionControl() {
  const [missions, setMissions] = useState<Mission[]>([]);
  const [agentStatus, setAgentStatus] = useState<AgentStatus | null>(null);
  const [showCreateForm, setShowCreateForm] = useState(false);
  const [loading, setLoading] = useState(true);
  const [formData, setFormData] = useState({
    title: "",
    targetRole: "",
    targetLocation: "",
    salaryMin: "",
    salaryMax: "",
    preferredSkills: "",
    experienceLevel: "MID",
    employmentType: "FULL_TIME",
    dailyLimit: "20",
    deadlineDays: "90",
  });

  useEffect(() => {
    fetchMissions();
    fetchAgentStatus();
  }, []);

  const fetchMissions = async () => {
    try {
      const response = await fetch("/api/v1/agent/missions/user/current");
      if (response.ok) {
        const data = await response.json();
        setMissions(data);
      }
    } catch (error) {
      console.error("Failed to fetch missions:", error);
    } finally {
      setLoading(false);
    }
  };

  const fetchAgentStatus = async () => {
    try {
      const response = await fetch("/api/v1/agent/status");
      if (response.ok) {
        const data = await response.json();
        setAgentStatus(data);
      }
    } catch (error) {
      console.error("Failed to fetch agent status:", error);
    }
  };

  const createMission = async () => {
    try {
      const response = await fetch("/api/v1/agent/missions", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          userId: "current",
          title: formData.title,
          targetRole: formData.targetRole,
          targetLocation: formData.targetLocation,
          salaryMin: formData.salaryMin ? parseInt(formData.salaryMin) : null,
          salaryMax: formData.salaryMax ? parseInt(formData.salaryMax) : null,
          preferredSkills: formData.preferredSkills.split(",").map((s) => s.trim()),
          experienceLevel: formData.experienceLevel,
          employmentType: formData.employmentType,
          dailyLimit: parseInt(formData.dailyLimit),
          deadlineDays: parseInt(formData.deadlineDays),
        }),
      });
      if (response.ok) {
        setShowCreateForm(false);
        fetchMissions();
        setFormData({
          title: "",
          targetRole: "",
          targetLocation: "",
          salaryMin: "",
          salaryMax: "",
          preferredSkills: "",
          experienceLevel: "MID",
          employmentType: "FULL_TIME",
          dailyLimit: "20",
          deadlineDays: "90",
        });
      }
    } catch (error) {
      console.error("Failed to create mission:", error);
    }
  };

  const startMission = async (missionId: string) => {
    try {
      await fetch(`/api/v1/agent/missions/${missionId}/start`, { method: "POST" });
      fetchMissions();
    } catch (error) {
      console.error("Failed to start mission:", error);
    }
  };

  const pauseMission = async (missionId: string) => {
    try {
      await fetch(`/api/v1/agent/missions/${missionId}/pause`, { method: "POST" });
      fetchMissions();
    } catch (error) {
      console.error("Failed to pause mission:", error);
    }
  };

  const cancelMission = async (missionId: string) => {
    try {
      await fetch(`/api/v1/agent/missions/${missionId}/cancel`, { method: "POST" });
      fetchMissions();
    } catch (error) {
      console.error("Failed to cancel mission:", error);
    }
  };

  const getStatusColor = (status: string) => {
    switch (status) {
      case "ACTIVE":
        return "bg-green-500";
      case "PAUSED":
        return "bg-yellow-500";
      case "COMPLETED":
        return "bg-blue-500";
      case "CANCELLED":
        return "bg-red-500";
      default:
        return "bg-gray-500";
    }
  };

  if (loading) {
    return (
      <div className="flex items-center justify-center h-64">
        <div className="text-lg">Loading Mission Control...</div>
      </div>
    );
  }

  return (
    <div className="container mx-auto p-6">
      <div className="flex items-center justify-between mb-6">
        <div>
          <h1 className="text-3xl font-bold">Mission Control</h1>
          <p className="text-muted-foreground">
            Autonomous AI Job Agent - Offline First
          </p>
        </div>
        <div className="flex items-center gap-4">
          {agentStatus && (
            <Badge variant={agentStatus.status === "running" ? "default" : "secondary"}>
              Agent: {agentStatus.status}
            </Badge>
          )}
          <Button onClick={() => setShowCreateForm(true)}>New Mission</Button>
        </div>
      </div>

      {showCreateForm && (
        <Card className="mb-6">
          <CardHeader>
            <CardTitle>Create New Mission</CardTitle>
            <CardDescription>
              Define your job search mission for the AI Agent
            </CardDescription>
          </CardHeader>
          <CardContent>
            <div className="grid grid-cols-2 gap-4">
              <div className="space-y-2">
                <label className="text-sm font-medium">Mission Title</label>
                <Input
                  placeholder="e.g., Find Remote Java Developer Role"
                  value={formData.title}
                  onChange={(e) => setFormData({ ...formData, title: e.target.value })}
                />
              </div>
              <div className="space-y-2">
                <label className="text-sm font-medium">Target Role</label>
                <Input
                  placeholder="e.g., Java Backend Developer"
                  value={formData.targetRole}
                  onChange={(e) => setFormData({ ...formData, targetRole: e.target.value })}
                />
              </div>
              <div className="space-y-2">
                <label className="text-sm font-medium">Target Location</label>
                <Input
                  placeholder="e.g., Remote, Bangalore"
                  value={formData.targetLocation}
                  onChange={(e) => setFormData({ ...formData, targetLocation: e.target.value })}
                />
              </div>
              <div className="space-y-2">
                <label className="text-sm font-medium">Preferred Skills (comma-separated)</label>
                <Input
                  placeholder="e.g., Java, Spring Boot, Microservices"
                  value={formData.preferredSkills}
                  onChange={(e) => setFormData({ ...formData, preferredSkills: e.target.value })}
                />
              </div>
              <div className="space-y-2">
                <label className="text-sm font-medium">Salary Min (LPA)</label>
                <Input
                  type="number"
                  placeholder="e.g., 15"
                  value={formData.salaryMin}
                  onChange={(e) => setFormData({ ...formData, salaryMin: e.target.value })}
                />
              </div>
              <div className="space-y-2">
                <label className="text-sm font-medium">Salary Max (LPA)</label>
                <Input
                  type="number"
                  placeholder="e.g., 25"
                  value={formData.salaryMax}
                  onChange={(e) => setFormData({ ...formData, salaryMax: e.target.value })}
                />
              </div>
              <div className="space-y-2">
                <label className="text-sm font-medium">Experience Level</label>
                <Select
                  value={formData.experienceLevel}
                  onValueChange={(value) => setFormData({ ...formData, experienceLevel: value })}
                >
                  <SelectTrigger>
                    <SelectValue />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value="FRESH">Fresh</SelectItem>
                    <SelectItem value="JUNIOR">Junior (0-2 years)</SelectItem>
                    <SelectItem value="MID">Mid (2-5 years)</SelectItem>
                    <SelectItem value="SENIOR">Senior (5+ years)</SelectItem>
                    <SelectItem value="LEAD">Lead</SelectItem>
                  </SelectContent>
                </Select>
              </div>
              <div className="space-y-2">
                <label className="text-sm font-medium">Employment Type</label>
                <Select
                  value={formData.employmentType}
                  onValueChange={(value) => setFormData({ ...formData, employmentType: value })}
                >
                  <SelectTrigger>
                    <SelectValue />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value="FULL_TIME">Full Time</SelectItem>
                    <SelectItem value="PART_TIME">Part Time</SelectItem>
                    <SelectItem value="CONTRACT">Contract</SelectItem>
                    <SelectItem value="INTERNSHIP">Internship</SelectItem>
                  </SelectContent>
                </Select>
              </div>
              <div className="space-y-2">
                <label className="text-sm font-medium">Daily Application Limit</label>
                <Input
                  type="number"
                  value={formData.dailyLimit}
                  onChange={(e) => setFormData({ ...formData, dailyLimit: e.target.value })}
                />
              </div>
              <div className="space-y-2">
                <label className="text-sm font-medium">Deadline (days)</label>
                <Input
                  type="number"
                  value={formData.deadlineDays}
                  onChange={(e) => setFormData({ ...formData, deadlineDays: e.target.value })}
                />
              </div>
            </div>
            <div className="flex justify-end gap-2 mt-4">
              <Button variant="outline" onClick={() => setShowCreateForm(false)}>
                Cancel
              </Button>
              <Button onClick={createMission}>Create Mission</Button>
            </div>
          </CardContent>
        </Card>
      )}

      <div className="grid gap-4">
        {missions.length === 0 ? (
          <Card>
            <CardContent className="flex flex-col items-center justify-center py-12">
              <p className="text-muted-foreground mb-4">No missions yet</p>
              <Button onClick={() => setShowCreateForm(true)}>Create Your First Mission</Button>
            </CardContent>
          </Card>
        ) : (
          missions.map((mission) => (
            <Card key={mission.id}>
              <CardHeader>
                <div className="flex items-center justify-between">
                  <div>
                    <CardTitle>{mission.title}</CardTitle>
                    <CardDescription>{mission.targetRole}</CardDescription>
                  </div>
                  <div className="flex items-center gap-2">
                    <Badge className={getStatusColor(mission.status)}>
                      {mission.status}
                    </Badge>
                    {mission.status === "CREATED" && (
                      <Button size="sm" onClick={() => startMission(mission.id)}>
                        Start
                      </Button>
                    )}
                    {mission.status === "ACTIVE" && (
                      <Button size="sm" variant="outline" onClick={() => pauseMission(mission.id)}>
                        Pause
                      </Button>
                    )}
                    {mission.status !== "COMPLETED" && mission.status !== "CANCELLED" && (
                      <Button size="sm" variant="destructive" onClick={() => cancelMission(mission.id)}>
                        Cancel
                      </Button>
                    )}
                  </div>
                </div>
              </CardHeader>
              <CardContent>
                <div className="grid grid-cols-4 gap-4 text-center">
                  <div>
                    <div className="text-2xl font-bold">{mission.totalJobsFound}</div>
                    <div className="text-sm text-muted-foreground">Jobs Found</div>
                  </div>
                  <div>
                    <div className="text-2xl font-bold">{mission.totalApplicationsSubmitted}</div>
                    <div className="text-sm text-muted-foreground">Applied</div>
                  </div>
                  <div>
                    <div className="text-2xl font-bold">{mission.totalPending}</div>
                    <div className="text-sm text-muted-foreground">Pending</div>
                  </div>
                  <div>
                    <div className="text-2xl font-bold">{mission.totalRejected}</div>
                    <div className="text-sm text-muted-foreground">Rejected</div>
                  </div>
                </div>
                <div className="mt-4 text-sm text-muted-foreground">
                  Daily Limit: {mission.dailyApplicationLimit} | 
                  Location: {mission.targetLocation || "Any"} |
                  Deadline: {mission.deadlineAt ? new Date(mission.deadlineAt).toLocaleDateString() : "N/A"}
                </div>
              </CardContent>
            </Card>
          ))
        )}
      </div>
    </div>
  );
}
