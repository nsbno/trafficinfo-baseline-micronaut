{
  "annotations": {
    "list": [
      {
        "builtIn": 1,
        "datasource": "-- Grafana --",
        "enable": true,
        "hide": true,
        "iconColor": "rgba(0, 211, 255, 1)",
        "name": "Annotations & Alerts",
        "type": "dashboard"
      },
      {
        "datasource": "${name_prefix}-${environment}",
        "dimensions": {
          "ServiceName": "${name_prefix}-${service_name}"
        },
        "enable": false,
        "iconColor": "rgba(255, 96, 96, 1)",
        "metricName": "*",
        "name": "Alarms",
        "namespace": "AWS/ECS",
        "period": "",
        "region": "eu-west-1",
        "statistics": [
          "Average"
        ]
      }
    ]
  },
  "editable": true,
  "gnetId": null,
  "graphTooltip": 0,
  "id": 45,
  "links": [],
  "panels": [
        {
          "content": "\n# Custom Dashboard for ${name_prefix}-${service_name}\n\nFor markdown syntax help: [commonmark.org/help](https://commonmark.org/help/)\n\n\n\n",
          "fieldConfig": {
            "defaults": {
              "custom": {}
            },
            "overrides": []
          },
          "gridPos": {
            "h": 4,
            "w": 24,
            "x": 0,
            "y": 0
          },
          "id": 5,
          "mode": "markdown",
          "targets": [
            {
              "alias": "",
              "dimensions": {},
              "expression": "",
              "id": "",
              "matchExact": true,
              "metricName": "",
              "namespace": "",
              "period": "",
              "refId": "A",
              "region": "default",
              "statistics": [
                "Average"
              ]
            }
          ],
          "timeFrom": null,
          "timeShift": null,
          "title": "Documentation",
          "type": "text",
          "datasource": null
        },
        {
			"aliasColors": {},
			"bars": false,
			"dashLength": 10,
			"dashes": false,
			"datasource": "${name_prefix}-${environment}",
			"editable": true,
			"error": false,
			"fieldConfig": {
				"defaults": {
					"custom": {}
				},
				"overrides": []
			},
			"fill": 2,
			"fillGradient": 0,
			"grid": {},
			"gridPos": {
				"h": 7,
				"w": 24,
				"x": 0,
				"y": 4
			},
			"hiddenSeries": false,
			"id": 1,
			"isNew": true,
			"legend": {
				"alignAsTable": true,
				"avg": true,
				"current": true,
				"max": true,
				"min": true,
				"rightSide": false,
				"show": true,
				"total": false,
				"values": true
			},
			"lines": true,
			"linewidth": 2,
			"links": [],
			"nullPointMode": "null",
			"options": {
				"dataLinks": []
			},
			"percentage": false,
			"pointradius": 5,
			"points": false,
			"renderer": "flot",
			"seriesOverrides": [],
			"spaceLength": 10,
			"stack": false,
			"steppedLine": false,
			"targets": [{
				"dimensions": {
					"ClusterName": "${name_prefix}-ecs-cluster",
					"ServiceName": "${name_prefix}-${service_name}"
				},
				"metricName": "CPUUtilization",
				"namespace": "AWS/ECS",
				"period": "",
				"refId": "A",
				"region": "eu-west-1",
				"statistics": [
					"Average"
				]
			}],
			"thresholds": [],
			"timeFrom": null,
			"timeRegions": [],
			"timeShift": null,
			"title": "CPUUtilization (cluster ${name_prefix}-ecs-cluster, service ${name_prefix}-${service_name})",
			"tooltip": {
				"msResolution": true,
				"shared": true,
				"sort": 0,
				"value_type": "cumulative"
			},
			"type": "graph",
			"xaxis": {
				"buckets": null,
				"mode": "time",
				"name": null,
				"show": true,
				"values": []
			},
			"yaxes": [{
					"format": "percent",
					"label": null,
					"logBase": 1,
					"max": 100,
					"min": 0,
					"show": true
				},
				{
					"format": "short",
					"label": null,
					"logBase": 1,
					"max": null,
					"min": null,
					"show": false
				}
			],
			"yaxis": {
				"align": false,
				"alignLevel": null
			}
		},
        {
          "aliasColors": {},
          "bars": false,
          "datasource": "${name_prefix}-${environment}",
          "editable": true,
          "error": false,
          "fill": 1,
          "grid": {},
          "id": 3,
          "isNew": true,
          "legend": {
            "alignAsTable": true,
            "avg": true,
            "current": true,
            "max": true,
            "min": true,
            "rightSide": false,
            "show": true,
            "total": false,
            "values": true
          },
          "lines": true,
          "linewidth": 2,
          "links": [],
          "nullPointMode": "null",
          "percentage": false,
          "pointradius": 5,
          "points": false,
          "renderer": "flot",
          "seriesOverrides": [],
          "stack": false,
          "steppedLine": false,
          "targets": [
            {
              "dimensions": {
                "ClusterName": "${name_prefix}-ecs-cluster",
                "ServiceName": "${name_prefix}-${service_name}"
              },
              "metricName": "MemoryUtilization",
              "namespace": "AWS/ECS",
              "period": "",
              "refId": "A",
              "region": "eu-west-1",
              "statistics": [
                "Average"
              ]
            }
          ],
          "timeFrom": null,
          "timeShift": null,
          "title": "MemoryUtilization (cluster ${name_prefix}-ecs-cluster, service ${name_prefix}-${service_name})",
          "tooltip": {
            "msResolution": true,
            "shared": true,
            "sort": 0,
            "value_type": "cumulative"
          },
          "type": "graph",
          "xaxis": {
            "show": true,
            "mode": "time",
            "name": null,
            "values": [],
            "buckets": null
          },
          "yaxes": [
            {
              "format": "percent",
              "label": null,
              "logBase": 1,
              "max": 100,
              "min": 0,
              "show": true
            },
            {
              "format": "short",
              "label": null,
              "logBase": 1,
              "max": null,
              "min": null,
              "show": false
            }
          ],
          "gridPos": {
            "x": 0,
            "y": 11,
            "w": 24,
            "h": 7
          },
          "options": {
            "dataLinks": []
          },
          "thresholds": [],
          "fieldConfig": {
            "defaults": {
              "custom": {}
            },
            "overrides": []
          },
          "yaxis": {
            "align": false,
            "alignLevel": null
          },
          "fillGradient": 0,
          "dashes": false,
          "hiddenSeries": false,
          "dashLength": 10,
          "spaceLength": 10,
          "timeRegions": []
        }
  ],
  "schemaVersion": 25,
  "style": "dark",
  "tags": [
    "terraform",
    "${name}",
    "${environment}",
    "${name_prefix}"
  ],
  "templating": {
    "list": []
  },
  "time": {
    "from": "now-6h",
    "to": "now"
  },
  "timepicker": {
    "refresh_intervals": [
      "10s",
      "30s",
      "1m",
      "5m",
      "15m",
      "30m",
      "1h",
      "2h",
      "1d"
    ]
  },
  "timezone": "",
  "title": "${name}",
  "uid": "${uuid}",
  "version": 1
}