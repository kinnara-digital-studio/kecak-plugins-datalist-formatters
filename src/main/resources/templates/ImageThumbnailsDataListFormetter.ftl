<#list links as item>
    <#assign thumbnail = item['thumbnail'] >
    <#assign fullsize = item['fullsize'] >

    <a href="javascript:void(0);" class="zoom-trigger" data-thumbnail="${thumbnail}" data-fullsize="${fullsize}" data-filename="${item?index}">
        <img src="${thumbnail}" class="zoom-image" style="width:${width!50}px;height:${height!50}px;" alt="imageThumb" />
    </a>
</#list>
<script>
$(document).ready(function () {
    $(document).on('click', '.zoom-trigger', function (event) {
        event.preventDefault(); // Prevent default behavior

        // Check if an overlay already exists
        if ($('#zoom-viewer').length > 0) {
            return; // Prevent creating multiple overlays
        }

        // Get the image source and filename
        const imgSrc = $(this).data('fullsize');
        const filename = $(this).data('filename');

        // Create the modal overlay
        const overlay = $('<div>', {
            id: 'zoom-viewer',
            css: {
                position: 'fixed',
                top: 0,
                left: 0,
                width: '100%',
                height: '100%',
                backgroundColor: 'rgba(0,0,0,0.8)',
                display: 'flex',
                flexDirection: 'column',
                justifyContent: 'center',
                alignItems: 'center',
                zIndex: 1000
            },
            click: function () {
                $(this).remove(); // Close the overlay when clicked
            }
        });

        // Create the enlarged image
        const enlargedImg = $('<img>', {
            src: imgSrc,
            css: {
                maxWidth: '90%',
                maxHeight: '90%',
                border: '2px solid white',
                marginBottom: '10px'
            },
            click: function (e) {
                e.stopPropagation(); // Prevent the overlay from closing when clicking the image
            }
        });

        // Create the filename text
        const filenameText = $('<div>', {
            text: filename,
            css: {
                color: 'white',
                fontSize: '16px',
                textAlign: 'center'
            }
        });

        // Append the enlarged image and filename to the overlay
        overlay.append(enlargedImg).append(filenameText);

        // Append the overlay to the body
        $('body').append(overlay);
    });
});

</script>
