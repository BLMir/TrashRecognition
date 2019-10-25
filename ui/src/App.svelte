<script>
  let imageSrc = "about:blank"
  let errorText = ""
  let uploadContentSectionHidden = false
  let uploadedContentSectionHidden = true
  let imageResult

  async function onChangeTakePicture(event) {
    let file = await resolveFile(event)
    let { responseText } = await doRequest(file)

    let response = JSON.parse(responseText)

    if (response.length) {
      let guess = response[0]
      console.log(`It is ${guess.trashCategory} with ${Math.ceil(guess.accuracy)}%`)

      // [{"accuracy":59.76182818412781,"trashCategory":"PLASTIC"},{"accuracy":28.87408137321472,"trashCategory":"TRASH"},{"accuracy":11.353419721126556,"trashCategory":"PAPER"},{"accuracy":0.009257901547243819,"trashCategory":"CARDBOARD"},{"accuracy":0.001283600795431994,"trashCategory":"GLASS"},{"accuracy":1.2141181287006475E-4,"trashCategory":"METAL"}]
      switch(guess.trashCategory) {
        case 'PLASTIC':
          imageResult = '/plastic.png'
          break
        case 'TRASH':
          imageResult = '/organic.png'
          break
        case 'PAPER':
        case 'CARDBOARD':
          imageResult = '/cardboard.png'
          break
        case 'GLASS':
          imageResult = '/glass.png'
          break
        case 'METAL':
          imageResult = '/organic.png'
          break
      }
    }
  }

  function resolveFile(event) {
    return new Promise((resolve, reject) => {
      let files = event.target.files,
        file

      if (files && files.length > 0) {
        file = files[0]
        try {
          imageSrc = window.URL.createObjectURL(file)
          URL.revokeObjectURL(imgURL)

          uploadContentSectionHidden = true
          uploadedContentSectionHidden = false
          resolve(file)
        } catch (e) {
          try {
            let fileReader = new FileReader()

            fileReader.onload = function (event) {
                imageSrc = event.target.result
            }

            fileReader.readAsDataURL(file)

            uploadContentSectionHidden = true
            uploadedContentSectionHidden = false
            resolve(file)
          }
          catch (e) {
            errorText = "Neither createObjectURL or FileReader are supported"
            reject(errorText)
          }
        }
      } else {
        errorText = "No picture selected"
        reject(errorText)
      }
    })
  }

  function doRequest(file) {
    return new Promise((resolve, reject) => {
      let xhr = new XMLHttpRequest()

      xhr.addEventListener('progress', function(e) {
        let done = e.position || e.loaded, total = e.totalSize || e.total
        console.log('xhr progress: ' + (Math.floor(done/total*1000)/10) + '%')
      }, false)

      if ( xhr.upload ) {
        xhr.upload.onprogress = function(e) {
          let done = e.position || e.loaded, total = e.totalSize || e.total
          console.log('xhr.upload progress: ' + done + ' / ' + total + ' = ' + (Math.floor(done/total*1000)/10) + '%')
        }
      }

      xhr.onreadystatechange = function(e) {
        if ( 4 == this.readyState ) {
          console.log(['xhr upload complete', e])
          resolve(e.target)
        }
      }

      xhr.open('post', 'http://lifk.es:7000/guess-photo', true)

      let formData = new FormData()
      formData.append("file", file)
      xhr.send(formData)
    })
  }
</script>

<style>
  h1 {
    text-align: center;
  }

  .upload-content > p,
  .uploaded-content > p {
    text-align: center;
  }

  input[type=file] {
    width: 0.1px;
    height: 0.1px;
    opacity: 0;
    overflow: hidden;
    position: absolute;
    z-index: -1;
  }

  input[type=file] + label {
    font-size: 1.25em;
    font-weight: 700;
    color: white;
    background-color: black;
    display: inline-block;
    padding: 1.25em;
  }

  input[type=file]:focus + label,
  input[type=file] + label:hover {
    background-color: red;
  }

  input[type=file] + label {
    cursor: pointer;
  }

  input[type=file]:focus + label {
    outline: 1px dotted #000;
    outline: -webkit-focus-ring-color auto 5px;
  }

  input[type=file] + label * {
    pointer-events: none;
  }

  label > img {
    width: 8em;
  }

  #show-picture {
    max-width: 10em;
  }

  .hidden {
    display: none;
  }
</style>

<div class="container">
  <h1>Trash Recognition</h1>

  <section class="upload-content" class:hidden={ uploadContentSectionHidden === true }>
    <h2>Upload Image</h2>
    <p>
        <input type="file" id="take-picture" accept="image/*" on:change={onChangeTakePicture}>
        <label for="take-picture">
          <img src="/trash-recon-icon.png" alt="" />
        </label>
    </p>
  </section>
  <section class="uploaded-content" class:hidden={ uploadedContentSectionHidden === true }>
    <p>
      <img src={imageSrc} alt="" id="show-picture" />
    </p>
    <p>
      <img src={imageResult} alt="" id="result" />
    </p>

    <p id="error">{errorText}</p>
  </section>

  <p class="footer">All the code is available in the <a href="https://github.com/BLMir/TrashRecognition">Trash Recognition repository on GitHub</a>.</p>
</div>
