job "trash-hackathon" {
  datacenters = ["campus"]
  region = "DEV"
  type = "service"

  group "trash-hackathon" {

    count = 1

    task "trash-hackathon-task" {

      driver = "docker"

      service {
        tags = ["trv-net-dev-internal",
          "trv-env-dev"]
        name = "trash-api"
        port = "http"
      }

      config {
        image = "artifactory.tcs.trv.cloud:9090/trash-recognition:latest"
        port_map = {
          http = 7000
        }
      }

      resources {
        memory = "15000"

        network {
          port "http" {}
        }
      }
    }
  }
}