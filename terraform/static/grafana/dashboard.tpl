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
        "datasource": "$datasource",
        "dimensions": {
          "ServiceName": "$service"
        },
        "enable": false,
        "iconColor": "rgba(255, 96, 96, 1)",
        "metricName": "*",
        "name": "Alarms",
        "namespace": "AWS/ECS",
        "period": "",
        "region": "$region",
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
  "panels": [{
			"aliasColors": {},
			"bars": false,
			"dashLength": 10,
			"dashes": false,
			"datasource": "trafficinfo-prod",
			"editable": true,
			"error": false,
			"fieldConfig": {
				"defaults": {
					"custom": {}
				},
				"overrides": []
			},
			"fill": 1,
			"fillGradient": 0,
			"grid": {},
			"gridPos": {
				"h": 7,
				"w": 24,
				"x": 0,
				"y": 0
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
					"ClusterName": "trafficinfo-ecs-cluster",
					"ServiceName": "trafficinfo-baseline-micronaut"
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
			"title": "CPUUtilization (cluster trafficinfo-ecs-cluster, service trafficinfo-baseline-micronaut)",
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