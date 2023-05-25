<style>

#mapid {
  height: 900px;
}

</style>

<template>

  <div id="app">
    <div class="container">
      <h1 class="header">Solar Site Map</h1>
    </div>
    <div class="container">
      <form id="search" style="margin: 0.5em;" v-on:submit.prevent="submitForm($event)">
        <div class="form-row m-1">
          <div class="col px-1">
            <input id="lat" class="form-control" name="latitude" placeholder="Latitude" type="text">
          </div>
          <div class="col px-1">
            <input id="lng" class="form-control" name="longitude" placeholder="Longitude" type="text">
          </div>
          <div class="col-sm-1 px-1">
            <input class="form-control" name="radius" placeholder="Radius" type="text">
          </div>
          <div class="col2 px-1">
            <select class="custom-select" name="radiusUnit">
              <option selected="selected" value="KM">KM (Kilometers)</option>
              <option value="MI">MI (Miles)</option>
            </select>
          </div>
          <div class="col2 px-1 form-check form-check-inline">
            <input id="excessCapacityCheck" class="form-check-input" name="onlyExcessCapacity" type="checkbox">
            <label class="form-check-label" for="excessCapacityCheck">Excess Capacity</label>
          </div>
          <div class="col2 px-1">
            <button class="btn btn-primary" type="submit">Submit</button>
          </div>
        </div>
      </form>
    </div>
    <div id="mapid" class="container"></div>
  </div>

</template>

<script>
import {L, LMap, LMarker, LTileLayer} from 'vue2-leaflet'
import axios from 'axios'

export default {
  name: 'App',
  components: {
    LMap,
    LTileLayer,
    LMarker
  },
  mounted() {
    this.createMap()
    this.getData()
  },
  methods: {
    submitForm(event) {
      this.markerLayers.clearLayers()
      console.log(event.srcElement)

      const args = {
        params: {
          lat: event.target.lat.value,
          lng: event.target.lng.value,
          radius: event.target.radius.value,
          radiusUnit: event.target.radiusUnit.value,
          onlyExcessCapacity: event.target.onlyExcessCapacity.checked
        }
      }
      const bounds = []
      axios.get(`${process.env.apiHost}api/sites`, args)
        .then((response) => {
          response.data.forEach((site) => {
            this.addMarker(site)
            bounds.push([site.coordinate.lat, site.coordinate.lng])
          })
          this.mymap.fitBounds(bounds)
        })
        .catch(function (error) {
          console.log(error)
        })
    },
    getData() {
      var self = this
      axios.get(`${process.env.apiHost}api/sites`)
        .then(function (response) {
          response.data.forEach(function (site) {
            self.addMarker(site)
          })
        })
        .catch(function (error) {
          console.log(error)
        })
    },
    addMarker(site) {
      const coordinate = site.coordinate
      const marker = L.marker([coordinate.lat, coordinate.lng]).addTo(this.markerLayers)
      marker.bindPopup(`<b>${site.address}</b><br/>${site.city}, ${site.state} ${site.postalCode}<br>(${site.coordinate.lat}, ${site.coordinate.lng})<br/><a href="#/stats/${site.id}">Stats</a>`)
    },
    createMap() {
      this.mymap = L.map('mapid').setView([37.715732, -122.027342], 11)
      this.markerLayers = L.featureGroup().addTo(this.mymap)
      L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png?',
        {attribution: 'Map and Image data &copy; <a href="https://www.openstreetmap.org/">OpenStreetMap</a> contributors. <a href="https://www.openstreetmap.org/copyright">License</a>.'}
      ).addTo(this.mymap)
    }
  }
}

</script>
